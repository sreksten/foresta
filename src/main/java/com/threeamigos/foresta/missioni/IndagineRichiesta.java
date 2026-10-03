package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Un'indagine (vedi LIndagine), letta da una riga di INDAGINE in missioni.txt, che ne descrive i campi: chi la chiede,
 * dove cercare gli indizi e che cosa dicono, fra chi scegliere il colpevole, dove si nasconde, con chi, quanto si paga
 * e i testi. Nei testi %CAPO% è il nome del capo, se c'è (vedi CapoDellaRiga).
 */
public final class IndagineRichiesta {

	/**
	 * Dove si possono cercare gli indizi: i posti dei combattimenti, più templi e locande.
	 */
	static final Set<ClassiLocazione> POSTI_DEGLI_INDIZI;
	static final String CAPO = CombattimentoRichiesto.CAPO;
	private static final String SEPARATORE_DEL_LUOGO = ":";
	private static final String SEPARATORE_DEI_SOSPETTI = "/";
	private static final int INDIZI_MASSIMI = 3;
	private static final int SOSPETTI_MINIMI = 2;
	private static final int SOSPETTI_MASSIMI = 5;

	static {
		Set<ClassiLocazione> posti = EnumSet.copyOf(CombattimentoRichiesto.LUOGHI);
		posti.add(ClassiLocazione.TEMPIO);
		posti.add(ClassiLocazione.LOCANDA);
		POSTI_DEGLI_INDIZI = Collections.unmodifiableSet(posti);
	}

	/**
	 * Un indizio: dove si trova e che cosa dice. Scritto LUOGO:testo, con il luogo fra i POSTI_DEGLI_INDIZI; lo usano
	 * anche i reperti della documentazione (vedi DocumentazioneRichiesta).
	 */
	public static final class Indizio {

		private final ClassiLocazione luogo;
		private final String testo;

		private Indizio(ClassiLocazione luogo, String testo) {
			this.luogo = luogo;
			this.testo = testo;
		}

		/**
		 * L'indizio di un campo LUOGO:testo della riga.
		 */
		static Indizio da(String campo, String riga) {
			int separatore = campo.indexOf(SEPARATORE_DEL_LUOGO);
			if (separatore < 0) {
				throw new IllegalArgumentException("Un indizio è LUOGO:testo: " + campo + " in " + riga);
			}
			ClassiLocazione posto = ClassiLocazione.valueOf(campo.substring(0, separatore).trim());
			if (!POSTI_DEGLI_INDIZI.contains(posto)) {
				throw new IllegalArgumentException("Gli indizi stanno fra " + POSTI_DEGLI_INDIZI + ": " + riga);
			}
			return new Indizio(posto, campo.substring(separatore + 1).trim());
		}

		public ClassiLocazione getLuogo() {
			return luogo;
		}

		public String getTesto() {
			return testo;
		}
	}

	private final String riga;
	private final String chiave;
	private final TipoMissione tipo;
	private final AspettoDelMandante aspetto;
	private final String mandante;
	private final List<Indizio> indizi = new ArrayList<>();
	private final String domanda;
	private final List<String> sospetti;
	private final int colpevole;
	private final ClassePersonaggio nemico;
	private final int numero;
	private final CapoDellaRiga capo;
	private final boolean finoAllaResa;
	private final ClassiLocazione luogo;
	private final int monete;
	private final String titolo;
	private final String richiesta;
	private final String battutaDelCapo;
	private final String risposta;
	private final String smascheramento;
	private final String errore;
	private final String vittoria;
	private final String ringraziamento;
	private final String ricordo;

	private IndagineRichiesta(String riga) {
		this.riga = riga;
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, "CHIAVE", "TIPO", "ASPETTO", "MANDANTE", "INDIZIO_1", "INDIZIO_2",
				"INDIZIO_3", "DOMANDA", "SOSPETTI", "COLPEVOLE", "NEMICO", "NUMERO", "CAPO", "RESA", "LUOGO", "MONETE", "TITOLO",
				"RICHIESTA", "BATTUTA", "RISPOSTA", "SMASCHERAMENTO", "ERRORE", "VITTORIA", "RINGRAZIAMENTO", "RICORDO");
		chiave = campi.obbligatorio("CHIAVE");
		tipo = campi.enumerato("TIPO", TipoMissione.class);
		aspetto = campi.enumerato("ASPETTO", AspettoDelMandante.class);
		mandante = campi.obbligatorio("MANDANTE");
		indizi.add(indizio(campi.obbligatorio("INDIZIO_1")));
		indizi.add(indizio(campi.obbligatorio("INDIZIO_2")));
		Optional<String> terzo = campi.facoltativo("INDIZIO_" + INDIZI_MASSIMI);
		terzo.ifPresent(indizio -> indizi.add(indizio(indizio)));
		domanda = campi.obbligatorio("DOMANDA");
		List<String> elenco = new ArrayList<>();
		for (String sospetto : campi.obbligatorio("SOSPETTI").split(SEPARATORE_DEI_SOSPETTI)) {
			elenco.add(sospetto.trim());
		}
		sospetti = Collections.unmodifiableList(elenco);
		if (sospetti.size() < SOSPETTI_MINIMI || sospetti.size() > SOSPETTI_MASSIMI || sospetti.contains("")) {
			throw new IllegalArgumentException("I sospetti sono da " + SOSPETTI_MINIMI + " a " + SOSPETTI_MASSIMI + ": " + riga);
		}
		colpevole = campi.intero("COLPEVOLE");
		if (colpevole < 1 || colpevole > sospetti.size()) {
			throw new IllegalArgumentException("Il colpevole è il numero di uno dei sospetti: " + riga);
		}
		nemico = campi.enumerato("NEMICO", ClassePersonaggio.class);
		numero = campi.intero("NUMERO");
		capo = CapoDellaRiga.da(campi.facoltativo("CAPO"));
		finoAllaResa = campi.facoltativo("RESA").map("SI"::equals).orElse(false);
		luogo = campi.enumerato("LUOGO", ClassiLocazione.class);
		if (!CombattimentoRichiesto.LUOGHI.contains(luogo)) {
			throw new IllegalArgumentException("Il luogo è fra " + CombattimentoRichiesto.LUOGHI + ": " + riga);
		}
		monete = campi.intero("MONETE");
		if (numero < 1 || monete < 1) {
			throw new IllegalArgumentException("Numero e monete sono almeno 1: " + riga);
		}
		titolo = campi.obbligatorio("TITOLO");
		richiesta = campi.obbligatorio("RICHIESTA");
		battutaDelCapo = campi.obbligatorio("BATTUTA");
		risposta = campi.obbligatorio("RISPOSTA");
		smascheramento = campi.obbligatorio("SMASCHERAMENTO");
		errore = campi.obbligatorio("ERRORE");
		vittoria = campi.obbligatorio("VITTORIA");
		ringraziamento = campi.obbligatorio("RINGRAZIAMENTO");
		ricordo = campi.obbligatorio("RICORDO");
	}

	private Indizio indizio(String campo) {
		return Indizio.da(campo, riga);
	}

	public static IndagineRichiesta da(String riga) {
		return new IndagineRichiesta(riga);
	}

	public String getRiga() {
		return riga;
	}

	public String getChiave() {
		return chiave;
	}

	/**
	 * Il tipo di missione che l'indagine copre (investigazione, testimoni, controspionaggio...).
	 */
	public TipoMissione getTipo() {
		return tipo;
	}

	AspettoDelMandante getAspetto() {
		return aspetto;
	}

	/**
	 * Chi la chiede, con l'articolo: "il sacerdote".
	 */
	public String getMandante() {
		return mandante;
	}

	/**
	 * Gli indizi, due o tre, nell'ordine in cui si trovano.
	 */
	public List<Indizio> getIndizi() {
		return Collections.unmodifiableList(indizi);
	}

	/**
	 * La domanda che si pone al giocatore dopo l'ultimo indizio: "Chi ha rubato le campane?".
	 */
	public String getDomanda() {
		return domanda;
	}

	/**
	 * I sospetti fra cui scegliere, nell'ordine.
	 */
	public List<String> getSospetti() {
		return sospetti;
	}

	/**
	 * Il numero del colpevole fra i sospetti, da 1.
	 */
	public int getColpevole() {
		return colpevole;
	}

	public ClassePersonaggio getNemico() {
		return nemico;
	}

	public int getNumero() {
		return numero;
	}

	public boolean isConCapo() {
		return capo != null;
	}

	/**
	 * Il nome del capo: scritto nella riga, o pescato dalla sua produzione (vedi CapoDellaRiga). Solo se c'è un capo.
	 */
	public String pescaNomeDelCapo() {
		return capo.pescaNome();
	}

	/**
	 * Dove si nasconde il colpevole.
	 */
	public ClassiLocazione getLuogo() {
		return luogo;
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
		return battutaDelCapo;
	}

	public String getRisposta() {
		return risposta;
	}

	/**
	 * Che cosa si scrive quando si sceglie il colpevole giusto.
	 */
	public String getSmascheramento() {
		return smascheramento;
	}

	/**
	 * Che cosa si scrive quando si sceglie un innocente: la missione fallisce.
	 */
	public String getErrore() {
		return errore;
	}

	/**
	 * Che cosa si scrive quando il colpevole è stato sconfitto.
	 */
	public String getVittoria() {
		return vittoria;
	}

	public String getRingraziamento() {
		return ringraziamento;
	}

	/**
	 * Il ricordo del covo del colpevole, a missione finita.
	 */
	public String getRicordo() {
		return ricordo;
	}

	/**
	 * Se si combatte fino alla resa e non all'ultimo sangue (RESA=SI): vedi IncontroDiMissione.finoAllaResa.
	 */
	public boolean isFinoAllaResa() {
		return finoAllaResa;
	}

}
