package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Una benedizione (vedi LaBenedizione), letta da una riga di BENEDIZIONE in missioni.txt, che ne descrive i campi: chi
 * la offre in una locanda, il favore che chiede in cambio (vedi FavoreRichiesto e IlFavore), la benedizione che dà in
 * un tempio e i testi. Nei testi %PERSONAGGIO% è il nome di chi riceve la benedizione.
 */
public final class BenedizioneRichiesta {

	static final String PERSONAGGIO = "%PERSONAGGIO%";
	private static final String SACERDOTESSA = "SACERDOTESSA";
	private static final String SACERDOTE = "SACERDOTE";

	private final String riga;
	private final String chiave;
	private final TipoPersonaggio classe;
	private final String mandante;
	private final String nomeDellaBenedizione;
	private final ModificatoreDellaRiga modificatore;
	private final String richiesta;
	private final String battutaDelCapo;
	private final String risposta;
	private final FavoreRichiesto favore;
	private final String benedetto;

	private BenedizioneRichiesta(String riga) {
		this.riga = riga;
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, FavoreRichiesto.conICampiDelFavore("CHIAVE", "ASPETTO", "MANDANTE",
				"NOME", "BENEDIZIONE", "RICHIESTA", "BATTUTA", "RISPOSTA", "BENEDETTO"));
		chiave = campi.obbligatorio("CHIAVE");
		String aspetto = campi.obbligatorio("ASPETTO");
		if (!SACERDOTESSA.equals(aspetto) && !SACERDOTE.equals(aspetto)) {
			throw new IllegalArgumentException("L'aspetto è SACERDOTE o SACERDOTESSA: " + riga);
		}
		classe = SACERDOTESSA.equals(aspetto) ? TipoPersonaggio.SACERDOTESSA : TipoPersonaggio.SACERDOTE;
		mandante = campi.obbligatorio("MANDANTE");
		nomeDellaBenedizione = campi.obbligatorio("NOME");
		modificatore = ModificatoreDellaRiga.da(campi.obbligatorio("BENEDIZIONE"), riga);
		richiesta = campi.obbligatorio("RICHIESTA");
		battutaDelCapo = campi.obbligatorio("BATTUTA");
		risposta = campi.obbligatorio("RISPOSTA");
		favore = new FavoreRichiesto(campi, riga);
		benedetto = campi.obbligatorio("BENEDETTO");
	}

	public static BenedizioneRichiesta da(String riga) {
		return new BenedizioneRichiesta(riga);
	}

	public String getRiga() {
		return riga;
	}

	public String getChiave() {
		return chiave;
	}

	/**
	 * La classe di chi offre la benedizione: SACERDOTESSA o SACERDOTE.
	 */
	public TipoPersonaggio getClasse() {
		return classe;
	}

	/**
	 * Chi la offre, con l'articolo: "la sacerdotessa della luna".
	 */
	public String getMandante() {
		return mandante;
	}

	/**
	 * Il nome della benedizione, che resta scritto nel modificatore: "la benedizione della luna".
	 */
	public String getNomeDellaBenedizione() {
		return nomeDellaBenedizione;
	}

	/**
	 * Il modificatore permanente che la benedizione dà a chi la riceve.
	 */
	public ModificatoreAttributoMD nuovoModificatore() {
		return modificatore.nuovo(nomeDellaBenedizione);
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
	 * Il favore che chiede in cambio.
	 */
	public FavoreRichiesto getFavore() {
		return favore;
	}

	/**
	 * Che cosa si scrive quando un personaggio riceve la benedizione, con il suo nome al posto di %PERSONAGGIO%.
	 */
	public String getBenedetto() {
		return benedetto;
	}
}
