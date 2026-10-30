package com.threeamigos.foresta.missioni.richieste;

import com.threeamigos.foresta.tipi.AspettoDelMandante;
import com.threeamigos.foresta.tipi.ClasseDiPosto;
import com.threeamigos.foresta.tipi.TipoMissione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Una missione a tappe (vedi missione_a_tappe.md), letta da una riga di tappe.txt: una catena di posti in cui ogni tappa, una volta
 * raggiunta, dà un'informazione su dove andare dopo, fino all'ultimo posto, dove sta ciò che si cerca (una persona o un leggendario).
 * Può avere, al posto di una catena semplice, la scelta fra due piste (un posto giusto e un vicolo cieco) o le tre chiavi da recuperare
 * (tre ricerche in qualunque ordine, una delle quali può essere uno scambio invece di un combattimento); le farse hanno un secondo
 * scambio di battute con il mandante e al più due posti.
 * <p>
 * I testi hanno dei segnaposto che la missione risolve mentre si gioca, perché i nomi dei posti nascono con la mappa: {@link #PROSSIMO} e le sue
 * forme con la preposizione, per il bivio {@link #PISTA_A} e {@link #PISTA_B} (con le loro forme) e {@link #A_ALTRA}, {@link #OGGETTO} e
 * {@link #DI_OGGETTO}, {@link #CAPO}, {@link #MONETE} (la paga vera: il campo MONETE è la base, che si moltiplica per il livello del mondo di quando si
 * offre la missione) e {@code %POSTO_n%}.
 */
public final class TappeRichieste {

	public static final String PROSSIMO = "%PROSSIMO%";
	public static final String A_PROSSIMO = "%A_PROSSIMO%";
	public static final String DI_PROSSIMO = "%DI_PROSSIMO%";
	public static final String DA_PROSSIMO = "%DA_PROSSIMO%";
	public static final String PISTA_A = "%PISTA_A%";
	public static final String PISTA_B = "%PISTA_B%";
	public static final String A_ALTRA = "%A_ALTRA%";
	public static final String OGGETTO = "%OGGETTO%";
	public static final String DI_OGGETTO = "%DI_OGGETTO%";
	public static final String CAPO = "%CAPO%";
	public static final String MONETE = "%MONETE%";

	/**
	 * Le tappe, arrivo escluso, che una riga può avere.
	 */
	public static final int TAPPE_MASSIME = 5;
	/**
	 * Quanti sono i custodi delle chiavi.
	 */
	public static final int CUSTODI = 3;
	/**
	 * Quanti posti, al massimo, ha una farsa: una tappa e l'arrivo.
	 */
	public static final int POSTI_MASSIMI_DI_UNA_FARSA = 2;

	private static final String SCAMBIO = "SCAMBIO";
	private static final String LEGGENDARIO = "LEGGENDARIO";
	private static final String SEPARATORE_DEL_TESTO = ":";
	private static final String SEPARATORE_DELLA_SCENA = "/";
	private static final Pattern SEGNAPOSTO = Pattern.compile("%[A-Z_0-9]+%");
	private static final Set<String> SEGNAPOSTO_AMMESSI;

	static {
		Set<String> ammessi = new HashSet<>(Arrays.asList(PROSSIMO, A_PROSSIMO, DI_PROSSIMO, DA_PROSSIMO, PISTA_A, PISTA_B, A_ALTRA,
				OGGETTO, DI_OGGETTO, CAPO, MONETE, "%A_PISTA_A%", "%DI_PISTA_A%", "%A_PISTA_B%", "%DI_PISTA_B%"));
		for (int i = 1; i <= CUSTODI; i++) {
			ammessi.add("%POSTO_" + i + "%");
		}
		SEGNAPOSTO_AMMESSI = Collections.unmodifiableSet(ammessi);
	}

	/**
	 * Un posto: la classe e, se c'è, chi lo custodisce e quanti sono. Scritto {@code CLASSE} o {@code CLASSE NEMICO NUMERO}.
	 */
	public static final class Posto {

		private final ClasseDiPosto classe;
		private final TipoPersonaggio guardia;
		private final int numero;

		private Posto(ClasseDiPosto classe, TipoPersonaggio guardia, int numero) {
			this.classe = classe;
			this.guardia = guardia;
			this.numero = numero;
		}

		static Posto da(String testo, String riga) {
			String[] parti = testo.trim().split("\\s+");
			if (parti.length != 1 && parti.length != 3) {
				throw new IllegalArgumentException("Un posto è CLASSE o CLASSE NEMICO NUMERO: " + testo + " in " + riga);
			}
			ClasseDiPosto classe = ClasseDiPosto.valueOf(parti[0]);
			if (parti.length == 1) {
				return new Posto(classe, null, 0);
			}
			int numero = Integer.parseInt(parti[2]);
			if (numero < 1) {
				throw new IllegalArgumentException("Il numero di guardie è almeno 1: " + testo + " in " + riga);
			}
			if (classe.isCitta() || classe == ClasseDiPosto.LOCANDA) {
				throw new IllegalArgumentException("In una città o in una locanda non ci sono guardie da combattere: " + testo + " in " + riga);
			}
			return new Posto(classe, TipoPersonaggio.valueOf(parti[1]), numero);
		}

		public ClasseDiPosto getClasse() {
			return classe;
		}

		public boolean hasGuardia() {
			return guardia != null;
		}

		/**
		 * Chi custodisce il posto, se c'è.
		 */
		public Optional<TipoPersonaggio> getGuardia() {
			return Optional.ofNullable(guardia);
		}

		/**
		 * Quante guardie, zero se non ce ne sono.
		 */
		public int getNumero() {
			return numero;
		}
	}

	/**
	 * La scena d'arrivo di una tappa: la narrazione, la battuta del capo e, se il gruppo ha un compagno, la sua. Scritta
	 * {@code narrazione/battuta del capo/battuta di un compagno}.
	 */
	public static final class Scena {

		private final String narrazione;
		private final String battutaDelCapo;
		private final String battutaDiUnCompagno;

		private Scena(String narrazione, String battutaDelCapo, String battutaDiUnCompagno) {
			this.narrazione = narrazione;
			this.battutaDelCapo = battutaDelCapo;
			this.battutaDiUnCompagno = battutaDiUnCompagno;
		}

		static Scena da(String testo, String riga) {
			String[] parti = testo.split(SEPARATORE_DELLA_SCENA);
			if (parti.length < 2 || parti.length > 3) {
				throw new IllegalArgumentException("Una scena è narrazione/battuta del capo e, facoltativa, battuta di un compagno: " + testo + " in " + riga);
			}
			for (String parte : parti) {
				if (parte.trim().isEmpty()) {
					throw new IllegalArgumentException("Una scena non ha parti vuote: " + testo + " in " + riga);
				}
			}
			return new Scena(parti[0].trim(), parti[1].trim(), parti.length == 3 ? parti[2].trim() : null);
		}

		public String getNarrazione() {
			return narrazione;
		}

		public String getBattutaDelCapo() {
			return battutaDelCapo;
		}

		/**
		 * La battuta di un compagno, da dire solo se il gruppo ne ha uno.
		 */
		public Optional<String> getBattutaDiUnCompagno() {
			return Optional.ofNullable(battutaDiUnCompagno);
		}
	}

	/**
	 * Una tappa: il posto, l'informazione che si riceve arrivandoci e la scena dell'arrivo.
	 */
	public static class Tappa {

		private final Posto posto;
		private final String informazione;
		private final Scena scena;

		private Tappa(Posto posto, String informazione, Scena scena) {
			this.posto = posto;
			this.informazione = informazione;
			this.scena = scena;
		}

		public Posto getPosto() {
			return posto;
		}

		/**
		 * Il testo dell'informazione, che dice dove andare dopo.
		 */
		public String getInformazione() {
			return informazione;
		}

		public Scena getScena() {
			return scena;
		}
	}

	/**
	 * L'ultimo posto: dove sta ciò che si cerca. Con una guardia, la persona è prigioniera e va liberata.
	 */
	public static final class Arrivo {

		private final Posto posto;
		private final String testo;

		private Arrivo(Posto posto, String testo) {
			this.posto = posto;
			this.testo = testo;
		}

		public Posto getPosto() {
			return posto;
		}

		public String getTesto() {
			return testo;
		}
	}

	/**
	 * Le due piste fra cui il gruppo sceglie: una porta avanti, l'altra è un vicolo cieco che rimanda alla prima.
	 */
	public static final class Bivio {

		private final Tappa pistaA;
		private final Tappa pistaB;
		private final boolean giustaLaPrima;

		private Bivio(Tappa pistaA, Tappa pistaB, boolean giustaLaPrima) {
			this.pistaA = pistaA;
			this.pistaB = pistaB;
			this.giustaLaPrima = giustaLaPrima;
		}

		public Tappa getPistaA() {
			return pistaA;
		}

		public Tappa getPistaB() {
			return pistaB;
		}

		public Tappa getPistaGiusta() {
			return giustaLaPrima ? pistaA : pistaB;
		}

		/**
		 * La pista sbagliata: un vicolo cieco il cui testo manda all'altra ({@link TappeRichieste#A_ALTRA}).
		 */
		public Tappa getPistaFalsa() {
			return giustaLaPrima ? pistaB : pistaA;
		}
	}

	/**
	 * Quello che un custode vuole al posto di un combattimento: un oggetto, che sta in un posto con la sua guardia.
	 */
	public static final class Scambio {

		private final Posto posto;
		private final String oggetto;
		private final String testo;

		private Scambio(Posto posto, String oggetto, String testo) {
			this.posto = posto;
			this.oggetto = oggetto;
			this.testo = testo;
		}

		public Posto getPosto() {
			return posto;
		}

		/**
		 * Che cosa vuole, con l'articolo: "il medaglione".
		 */
		public String getOggetto() {
			return oggetto;
		}

		/**
		 * Dove sta e chi lo custodisce.
		 */
		public String getTesto() {
			return testo;
		}
	}

	/**
	 * Un custode di una chiave: dove sta, chi lo difende (o, se non combatte, che cosa vuole in cambio) e il suo capo con nome.
	 */
	public static final class Custode {

		private final ClasseDiPosto classe;
		private final TipoPersonaggio nemico;
		private final int numero;
		private final CapoDellaRiga capo;
		private final String testo;
		private final Scambio scambio;

		private Custode(ClasseDiPosto classe, TipoPersonaggio nemico, int numero, CapoDellaRiga capo, String testo, Scambio scambio) {
			this.classe = classe;
			this.nemico = nemico;
			this.numero = numero;
			this.capo = capo;
			this.testo = testo;
			this.scambio = scambio;
		}

		public ClasseDiPosto getClasse() {
			return classe;
		}

		public boolean isInScambio() {
			return scambio != null;
		}

		/**
		 * Chi lo difende, se combatte.
		 */
		public Optional<TipoPersonaggio> getNemico() {
			return Optional.ofNullable(nemico);
		}

		public int getNumero() {
			return numero;
		}

		public boolean isConCapo() {
			return capo != null;
		}

		/**
		 * Il nome del capo: scritto nella riga, o pescato (vedi CapoDellaRiga). Solo se c'è un capo.
		 */
		public String pescaNomeDelCapo() {
			return capo.pescaNome();
		}

		public String getTesto() {
			return testo;
		}

		/**
		 * Che cosa vuole in cambio della chiave, se non combatte.
		 */
		public Optional<Scambio> getScambio() {
			return Optional.ofNullable(scambio);
		}
	}

	private final String riga;
	private final String chiave;
	private final TipoMissione tipo;
	private final AspettoDelMandante aspetto;
	private final String mandante;
	private final int monete;
	private final String titolo;
	private final String richiesta;
	private final String battuta;
	private final String risposta;
	private final String battuta2;
	private final String risposta2;
	private final boolean oggettoLeggendario;
	private final List<Tappa> tappe = new ArrayList<>();
	private final Bivio bivio;
	private final List<String> oggettiChiave = new ArrayList<>();
	private final List<Custode> custodi = new ArrayList<>();
	private final Arrivo arrivo;
	private final String ringraziamento;
	private final String ricordo;

	private TappeRichieste(String riga) {
		this.riga = riga;
		List<String> ammessi = new ArrayList<>(Arrays.asList("CHIAVE", "TIPO", "ASPETTO", "MANDANTE", "MONETE", "TITOLO", "RICHIESTA", "BATTUTA",
				"RISPOSTA", "BATTUTA_2", "RISPOSTA_2", "OGGETTO", "ARRIVO", "RINGRAZIAMENTO", "RICORDO", "CHIAVI", "PISTA_A", "PISTA_B",
				"GIUSTA", "SCENA_A", "SCENA_B"));
		for (int n = 1; n <= TAPPE_MASSIME; n++) {
			ammessi.add("TAPPA_" + n);
			ammessi.add("SCENA_" + n);
		}
		for (int n = 1; n <= CUSTODI; n++) {
			ammessi.add("OGGETTO_CHIAVE_" + n);
			ammessi.add("CUSTODE_" + n);
			ammessi.add("SCAMBIO_" + n);
		}
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, ammessi.toArray(new String[0]));
		chiave = campi.obbligatorio("CHIAVE");
		tipo = campi.enumerato("TIPO", TipoMissione.class);
		aspetto = campi.enumerato("ASPETTO", AspettoDelMandante.class);
		mandante = campi.obbligatorio("MANDANTE");
		monete = campi.intero("MONETE");
		if (monete < 1) {
			throw new IllegalArgumentException("Le monete sono almeno 1: " + riga);
		}
		titolo = campi.obbligatorio("TITOLO");
		richiesta = campi.obbligatorio("RICHIESTA");
		battuta = campi.obbligatorio("BATTUTA");
		risposta = campi.obbligatorio("RISPOSTA");
		battuta2 = campi.facoltativo("BATTUTA_2").orElse(null);
		risposta2 = campi.facoltativo("RISPOSTA_2").orElse(null);
		if ((battuta2 == null) != (risposta2 == null)) {
			throw new IllegalArgumentException("Il secondo scambio ha la battuta e la risposta, o nessuna delle due: " + riga);
		}
		oggettoLeggendario = campi.facoltativo("OGGETTO").map(valore -> {
			if (!LEGGENDARIO.equals(valore)) {
				throw new IllegalArgumentException("OGGETTO è LEGGENDARIO: " + riga);
			}
			return true;
		}).orElse(false);

		for (int n = 1; n <= TAPPE_MASSIME && campi.facoltativo("TAPPA_" + n).isPresent(); n++) {
			tappe.add(tappa(campi.obbligatorio("TAPPA_" + n), campi.obbligatorio("SCENA_" + n)));
		}
		for (int n = tappe.size() + 1; n <= TAPPE_MASSIME; n++) {
			if (campi.facoltativo("TAPPA_" + n).isPresent() || campi.facoltativo("SCENA_" + n).isPresent()) {
				throw new IllegalArgumentException("Le tappe vanno da 1 senza salti: " + riga);
			}
		}

		boolean conBivio = campi.facoltativo("PISTA_A").isPresent() || campi.facoltativo("PISTA_B").isPresent();
		if (conBivio) {
			if (tappe.isEmpty()) {
				throw new IllegalArgumentException("Le due piste vengono dopo almeno una tappa: " + riga);
			}
			String giusta = campi.obbligatorio("GIUSTA");
			if (!"A".equals(giusta) && !"B".equals(giusta)) {
				throw new IllegalArgumentException("La pista giusta è A o B: " + riga);
			}
			bivio = new Bivio(tappa(campi.obbligatorio("PISTA_A"), campi.obbligatorio("SCENA_A")),
					tappa(campi.obbligatorio("PISTA_B"), campi.obbligatorio("SCENA_B")), "A".equals(giusta));
			if (bivio.getPistaA().getPosto().getClasse() == bivio.getPistaB().getPosto().getClasse()) {
				throw new IllegalArgumentException("Le due piste hanno classi diverse: " + riga);
			}
			if (!bivio.getPistaFalsa().getInformazione().contains(A_ALTRA) || bivio.getPistaGiusta().getInformazione().contains(A_ALTRA)) {
				throw new IllegalArgumentException("Il vicolo cieco rimanda all'altra pista, e quella giusta no: " + riga);
			}
		} else {
			bivio = null;
			if (campi.facoltativo("GIUSTA").isPresent() || campi.facoltativo("SCENA_A").isPresent() || campi.facoltativo("SCENA_B").isPresent()) {
				throw new IllegalArgumentException("GIUSTA e SCENA_A/B ci sono solo con le due piste: " + riga);
			}
		}

		Optional<String> chiavi = campi.facoltativo("CHIAVI");
		if (chiavi.isPresent()) {
			if (conBivio) {
				throw new IllegalArgumentException("Una riga ha le chiavi o le due piste, non tutte e due: " + riga);
			}
			if (Integer.parseInt(chiavi.get()) != CUSTODI) {
				throw new IllegalArgumentException("I custodi sono " + CUSTODI + ": " + riga);
			}
			int scambi = 0;
			for (int n = 1; n <= CUSTODI; n++) {
				oggettiChiave.add(campi.obbligatorio("OGGETTO_CHIAVE_" + n));
				Custode custode = custode(campi.obbligatorio("CUSTODE_" + n), campi.facoltativo("SCAMBIO_" + n));
				scambi += custode.isInScambio() ? 1 : 0;
				custodi.add(custode);
			}
			if (scambi > 1) {
				throw new IllegalArgumentException("Un solo custode al più vuole uno scambio: " + riga);
			}
			if (new HashSet<>(oggettiChiave).size() != CUSTODI) {
				throw new IllegalArgumentException("Le chiavi sono tutte diverse: " + riga);
			}
		} else {
			for (int n = 1; n <= CUSTODI; n++) {
				if (campi.facoltativo("OGGETTO_CHIAVE_" + n).isPresent() || campi.facoltativo("CUSTODE_" + n).isPresent()
						|| campi.facoltativo("SCAMBIO_" + n).isPresent()) {
					throw new IllegalArgumentException("I custodi ci sono solo con CHIAVI: " + riga);
				}
			}
		}

		String[] fine = parteConTesto(campi.obbligatorio("ARRIVO"));
		arrivo = new Arrivo(Posto.da(fine[0], riga), fine[1]);
		ringraziamento = campi.obbligatorio("RINGRAZIAMENTO");
		ricordo = campi.obbligatorio("RICORDO");

		if (isFarsa()) {
			if (oggettoLeggendario || bivio != null || !custodi.isEmpty() || getNumeroDiPosti() > POSTI_MASSIMI_DI_UNA_FARSA) {
				throw new IllegalArgumentException("Una farsa ha al più " + POSTI_MASSIMI_DI_UNA_FARSA + " posti, e niente leggendario, piste o chiavi: " + riga);
			}
		}
		if (!getUltimaRisposta().contains(DA_PROSSIMO)) {
			throw new IllegalArgumentException("La scena del mandante finisce dicendo da dove cominciare (" + DA_PROSSIMO + "): " + riga);
		}
		controllaISegnaposto();
	}

	/**
	 * Legge la riga della grammatica che descrive la missione.
	 *
	 * @throws IllegalArgumentException se la riga è sbagliata, o manca un campo obbligatorio
	 */
	public static TappeRichieste da(String riga) {
		return new TappeRichieste(riga);
	}

	private String[] parteConTesto(String campo) {
		int separatore = campo.indexOf(SEPARATORE_DEL_TESTO);
		if (separatore < 0) {
			throw new IllegalArgumentException("Un campo è posto:testo: " + campo + " in " + riga);
		}
		String testo = campo.substring(separatore + 1).trim();
		if (testo.isEmpty()) {
			throw new IllegalArgumentException("Il testo non può mancare: " + campo + " in " + riga);
		}
		return new String[] {campo.substring(0, separatore), testo};
	}

	private Tappa tappa(String campo, String scena) {
		String[] parti = parteConTesto(campo);
		return new Tappa(Posto.da(parti[0], riga), parti[1], Scena.da(scena, riga));
	}

	private Custode custode(String campo, Optional<String> scambio) {
		String[] parti = parteConTesto(campo);
		String[] testa = parti[0].trim().split("\\s+");
		if (testa.length != 4) {
			throw new IllegalArgumentException("Un custode è CLASSE NEMICO NUMERO CAPO:testo: " + campo + " in " + riga);
		}
		ClasseDiPosto classe = ClasseDiPosto.valueOf(testa[0]);
		CapoDellaRiga capo = "NO".equals(testa[3]) ? null : CapoDellaRiga.da(Optional.of(testa[3]));
		if (SCAMBIO.equals(testa[1])) {
			if (!scambio.isPresent()) {
				throw new IllegalArgumentException("Un custode in scambio dice che cosa vuole (SCAMBIO_n): " + campo + " in " + riga);
			}
			String[] cosa = parteConTesto(scambio.get());
			String[] oggettoETesto = cosa[1].split(SEPARATORE_DELLA_SCENA);
			if (oggettoETesto.length != 2 || oggettoETesto[0].trim().isEmpty() || oggettoETesto[1].trim().isEmpty()) {
				throw new IllegalArgumentException("Uno scambio è posto:oggetto/testo: " + scambio.get() + " in " + riga);
			}
			Posto posto = Posto.da(cosa[0], riga);
			if (!posto.hasGuardia()) {
				throw new IllegalArgumentException("L'oggetto dello scambio sta in un posto con la sua guardia: " + scambio.get() + " in " + riga);
			}
			return new Custode(classe, null, 0, capo, parti[1], new Scambio(posto, oggettoETesto[0].trim(), oggettoETesto[1].trim()));
		}
		if (scambio.isPresent()) {
			throw new IllegalArgumentException("SCAMBIO_n c'è solo per un custode in scambio: " + campo + " in " + riga);
		}
		int numero = Integer.parseInt(testa[2]);
		if (numero < 1) {
			throw new IllegalArgumentException("Il numero di avversari è almeno 1: " + campo + " in " + riga);
		}
		return new Custode(classe, TipoPersonaggio.valueOf(testa[1]), numero, capo, parti[1], null);
	}

	private void controllaISegnaposto() {
		List<String> testi = new ArrayList<>(Arrays.asList(richiesta, battuta, risposta, ringraziamento, ricordo, titolo, arrivo.getTesto()));
		if (battuta2 != null) {
			testi.add(battuta2);
			testi.add(risposta2);
		}
		for (Tappa tappa : tappe) {
			testi.add(tappa.getInformazione());
		}
		if (bivio != null) {
			testi.add(bivio.getPistaA().getInformazione());
			testi.add(bivio.getPistaB().getInformazione());
		}
		for (Custode custode : custodi) {
			testi.add(custode.getTesto());
			custode.getScambio().ifPresent(scambio -> testi.add(scambio.getTesto()));
		}
		for (String testo : testi) {
			Matcher segnaposto = SEGNAPOSTO.matcher(testo);
			while (segnaposto.find()) {
				if (!SEGNAPOSTO_AMMESSI.contains(segnaposto.group())) {
					throw new IllegalArgumentException("Segnaposto sconosciuto " + segnaposto.group() + " in " + riga);
				}
			}
		}
	}

	public String getRiga() {
		return riga;
	}

	public String getChiave() {
		return chiave;
	}

	public TipoMissione getTipo() {
		return tipo;
	}

	public AspettoDelMandante getAspetto() {
		return aspetto;
	}

	public String getMandante() {
		return mandante;
	}

	public int getMonete() {
		return monete;
	}

	public String getTitolo() {
		return titolo;
	}

	public String getRichiesta() {
		return richiesta;
	}

	public String getBattutaDelCapo() {
		return battuta;
	}

	public String getRisposta() {
		return risposta;
	}

	/**
	 * Il secondo scambio con il mandante, se la missione è una farsa: la battuta del capo.
	 */
	public Optional<String> getSecondaBattutaDelCapo() {
		return Optional.ofNullable(battuta2);
	}

	public Optional<String> getSecondaRisposta() {
		return Optional.ofNullable(risposta2);
	}

	/**
	 * L'ultima risposta del mandante: finisce dicendo da dove cominciare.
	 */
	public String getUltimaRisposta() {
		return risposta2 != null ? risposta2 : risposta;
	}

	/**
	 * Se la missione è una farsa: una richiesta senza senso, con un secondo scambio e al più due posti.
	 */
	public boolean isFarsa() {
		return battuta2 != null;
	}

	/**
	 * Se ciò che si cerca è un leggendario (lo pesca la missione); altrimenti è una persona.
	 */
	public boolean isDiUnLeggendario() {
		return oggettoLeggendario;
	}

	/**
	 * Le tappe prima dell'arrivo (e, se c'è, del bivio), in ordine.
	 */
	public List<Tappa> getTappe() {
		return Collections.unmodifiableList(tappe);
	}

	/**
	 * La scelta fra due piste, se c'è: viene dopo l'ultima tappa.
	 */
	public Optional<Bivio> getBivio() {
		return Optional.ofNullable(bivio);
	}

	public boolean hasChiavi() {
		return !custodi.isEmpty();
	}

	/**
	 * I nomi delle chiavi (o dei sigilli), uno per custode; vuota se non ci sono chiavi.
	 */
	public List<String> getOggettiChiave() {
		return Collections.unmodifiableList(oggettiChiave);
	}

	/**
	 * I custodi delle chiavi, in ordine; vuota se non ci sono chiavi.
	 */
	public List<Custode> getCustodi() {
		return Collections.unmodifiableList(custodi);
	}

	public Arrivo getArrivo() {
		return arrivo;
	}

	/**
	 * Quanti posti il gruppo visita al più: le tappe, i due del bivio se c'è (uno solo ne serve, se si sceglie giusto) e l'arrivo.
	 */
	public int getNumeroDiPosti() {
		return tappe.size() + (bivio != null ? 2 : 0) + 1;
	}

	public String getRingraziamento() {
		return ringraziamento;
	}

	public String getRicordo() {
		return ricordo;
	}
}
