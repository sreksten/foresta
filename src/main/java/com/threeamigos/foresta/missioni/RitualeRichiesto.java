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
 * Un rito (vedi IlRituale), letto da una riga di RITUALE in missioni.txt, che ne descrive i campi: chi lo chiede, dove
 * si celebra, che ingredienti servono, come si celebra (una conferma, o una scelta fra metodi di cui uno solo è
 * giusto), chi salta fuori a disturbarlo (se qualcuno salta fuori), quanto si paga e i testi. Nei testi %CAPO% è il
 * nome del capo, se c'è (vedi CapoDellaRiga).
 */
public final class RitualeRichiesto {

	/**
	 * Dove si può celebrare un rito: i posti dei combattimenti, più i templi.
	 */
	static final Set<ClassiLocazione> LUOGHI;
	static final String CAPO = CombattimentoRichiesto.CAPO;
	private static final String SEPARATORE_DEI_METODI = "/";
	private static final int METODI_MINIMI = 2;
	/**
	 * Uno in meno delle opzioni possibili di una scelta: l'ultima è sempre "non ancora".
	 */
	private static final int METODI_MASSIMI = 4;

	static {
		Set<ClassiLocazione> luoghi = EnumSet.copyOf(CombattimentoRichiesto.LUOGHI);
		luoghi.add(ClassiLocazione.TEMPIO);
		LUOGHI = Collections.unmodifiableSet(luoghi);
	}

	private final String riga;
	private final String chiave;
	private final TipoMissione tipo;
	private final AspettoDelMandante aspetto;
	private final String mandante;
	private final ClassiLocazione luogo;
	private final MaterialeRichiesto ingrediente;
	private final int quantita;
	private final String domanda;
	private final List<String> metodi;
	private final int metodo;
	private final ClassePersonaggio nemico;
	private final int numero;
	private final CapoDellaRiga capo;
	private final int monete;
	private final String titolo;
	private final String richiesta;
	private final String battutaDelCapo;
	private final String risposta;
	private final String rito;
	private final String vittoria;
	private final String errore;
	private final String ringraziamento;
	private final String ricordo;

	private RitualeRichiesto(String riga) {
		this.riga = riga;
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, "CHIAVE", "TIPO", "ASPETTO", "MANDANTE", "LUOGO", "GENERE",
				"INGREDIENTE", "INGREDIENTI", "DOVE", "QUANTITA", "DOMANDA", "METODI", "METODO", "NEMICO", "NUMERO", "CAPO",
				"MONETE", "TITOLO", "RICHIESTA", "BATTUTA", "RISPOSTA", "RITO", "VITTORIA", "ERRORE", "RINGRAZIAMENTO", "RICORDO");
		chiave = campi.obbligatorio("CHIAVE");
		tipo = campi.enumerato("TIPO", TipoMissione.class);
		aspetto = campi.enumerato("ASPETTO", AspettoDelMandante.class);
		mandante = campi.obbligatorio("MANDANTE");
		luogo = campi.enumerato("LUOGO", ClassiLocazione.class);
		if (!LUOGHI.contains(luogo)) {
			throw new IllegalArgumentException("Il luogo è fra " + LUOGHI + ": " + riga);
		}
		quantita = campi.intero("QUANTITA");
		ingrediente = MaterialeRichiesto.ingrediente(campi.obbligatorio("GENERE"), campi.obbligatorio("INGREDIENTE"),
				campi.obbligatorio("INGREDIENTI"), campi.obbligatorio("DOVE"), quantita);
		domanda = campi.obbligatorio("DOMANDA");
		Optional<String> elenco = campi.facoltativo("METODI");
		List<String> letti = new ArrayList<>();
		elenco.ifPresent(testo -> {
			for (String metodo : testo.split(SEPARATORE_DEI_METODI)) {
				letti.add(metodo.trim());
			}
		});
		metodi = Collections.unmodifiableList(letti);
		errore = campi.facoltativo("ERRORE").orElse(null);
		if (elenco.isPresent()) {
			metodo = campi.intero("METODO");
			if (metodi.size() < METODI_MINIMI || metodi.size() > METODI_MASSIMI || metodi.contains("")
					|| metodo < 1 || metodo > metodi.size() || errore == null) {
				throw new IllegalArgumentException("Da " + METODI_MINIMI + " a " + METODI_MASSIMI
						+ " metodi, il numero di quello giusto e il testo dell'errore: " + riga);
			}
		} else {
			metodo = 0;
			if (campi.facoltativo("METODO").isPresent() || errore != null) {
				throw new IllegalArgumentException("METODO ed ERRORE vanno con METODI: " + riga);
			}
		}
		nemico = campi.facoltativo("NEMICO").map(ClassePersonaggio::valueOf).orElse(null);
		numero = nemico == null ? 0 : campi.intero("NUMERO");
		capo = CapoDellaRiga.da(campi.facoltativo("CAPO"));
		vittoria = campi.facoltativo("VITTORIA").orElse(null);
		if (nemico == null ? campi.facoltativo("NUMERO").isPresent() || capo != null || vittoria != null
				: numero < 1 || vittoria == null) {
			throw new IllegalArgumentException("NEMICO, NUMERO e VITTORIA vanno insieme, e CAPO solo con loro: " + riga);
		}
		monete = campi.intero("MONETE");
		if (monete < 1) {
			throw new IllegalArgumentException("Le monete sono almeno 1: " + riga);
		}
		titolo = campi.obbligatorio("TITOLO");
		richiesta = campi.obbligatorio("RICHIESTA");
		battutaDelCapo = campi.obbligatorio("BATTUTA");
		risposta = campi.obbligatorio("RISPOSTA");
		rito = campi.obbligatorio("RITO");
		ringraziamento = campi.obbligatorio("RINGRAZIAMENTO");
		ricordo = campi.obbligatorio("RICORDO");
	}

	public static RitualeRichiesto da(String riga) {
		return new RitualeRichiesto(riga);
	}

	public String getRiga() {
		return riga;
	}

	public String getChiave() {
		return chiave;
	}

	/**
	 * Il tipo di missione che il rito copre (rituale, sigillo, benedizione, possessione...).
	 */
	public TipoMissione getTipo() {
		return tipo;
	}

	AspettoDelMandante getAspetto() {
		return aspetto;
	}

	/**
	 * Chi lo chiede, con l'articolo: "il sacerdote".
	 */
	public String getMandante() {
		return mandante;
	}

	/**
	 * Dove si celebra.
	 */
	public ClassiLocazione getLuogo() {
		return luogo;
	}

	/**
	 * L'ingrediente del rito: come si chiama e dove si trova.
	 */
	public MaterialeRichiesto getIngrediente() {
		return ingrediente;
	}

	/**
	 * Quanti ne servono.
	 */
	public int getQuantita() {
		return quantita;
	}

	/**
	 * La domanda che si pone nel luogo del rito, con gli ingredienti.
	 */
	public String getDomanda() {
		return domanda;
	}

	/**
	 * Se il rito si celebra scegliendo un metodo, e non solo confermando.
	 */
	public boolean isConMetodi() {
		return !metodi.isEmpty();
	}

	/**
	 * I metodi fra cui scegliere, nell'ordine; vuota se basta una conferma.
	 */
	public List<String> getMetodi() {
		return metodi;
	}

	/**
	 * Il numero del metodo giusto, da 1; 0 se basta una conferma.
	 */
	public int getMetodo() {
		return metodo;
	}

	/**
	 * Se il rito richiama qualcuno da sconfiggere.
	 */
	public boolean isConNemici() {
		return nemico != null;
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
	 * Che cosa si scrive quando il rito si celebra: è tutto, se non salta fuori nessuno, altrimenti chi salta fuori.
	 */
	public String getRito() {
		return rito;
	}

	/**
	 * Che cosa si scrive quando chi è saltato fuori è stato sconfitto, o null se nessuno salta fuori.
	 */
	public String getVittoria() {
		return vittoria;
	}

	/**
	 * Che cosa si scrive quando si sceglie il metodo sbagliato (la missione fallisce), o null se basta una conferma.
	 */
	public String getErrore() {
		return errore;
	}

	public String getRingraziamento() {
		return ringraziamento;
	}

	/**
	 * Il ricordo del luogo del rito, a missione finita.
	 */
	public String getRicordo() {
		return ricordo;
	}
}
