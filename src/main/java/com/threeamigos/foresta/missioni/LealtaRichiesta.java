package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;

/**
 * Il problema personale di un compagno (vedi LaLealta), letto da una riga di LEALTA in missioni.txt, che ne descrive i
 * campi: che cosa confida intorno al fuoco dell'accampamento, il favore che chiede al gruppo (vedi FavoreRichiesto e
 * IlFavore), come ringrazia quando è fatto e che cosa ci guadagna, un modificatore permanente. Nei testi
 * %PERSONAGGIO% è il nome del compagno.
 */
public final class LealtaRichiesta {

	static final String PERSONAGGIO = BenedizioneRichiesta.PERSONAGGIO;
	/**
	 * La nota del modificatore che il compagno riceve: chi ce l'ha non chiede più niente.
	 */
	public static final String NOTA = "la lealtà";

	private final String riga;
	private final String chiave;
	private final String confidenza;
	private final String battutaDelCapo;
	private final String risposta;
	private final FavoreRichiesto favore;
	private final String ringraziamento;
	private final ModificatoreDellaRiga modificatore;
	private final String leale;

	private LealtaRichiesta(String riga) {
		this.riga = riga;
		CampiDiGrammatica campi = CampiDiGrammatica.da(riga, FavoreRichiesto.conICampiDelFavore("CHIAVE", "CONFIDENZA", "BATTUTA",
				"RISPOSTA", "RINGRAZIAMENTO", "LEALTA", "LEALE"));
		chiave = campi.obbligatorio("CHIAVE");
		confidenza = campi.obbligatorio("CONFIDENZA");
		battutaDelCapo = campi.obbligatorio("BATTUTA");
		risposta = campi.obbligatorio("RISPOSTA");
		favore = new FavoreRichiesto(campi, riga);
		ringraziamento = campi.obbligatorio("RINGRAZIAMENTO");
		modificatore = ModificatoreDellaRiga.da(campi.obbligatorio("LEALTA"), riga);
		leale = campi.obbligatorio("LEALE");
	}

	public static LealtaRichiesta da(String riga) {
		return new LealtaRichiesta(riga);
	}

	public String getRiga() {
		return riga;
	}

	public String getChiave() {
		return chiave;
	}

	/**
	 * Che cosa confida il compagno intorno al fuoco.
	 */
	public String getConfidenza() {
		return confidenza;
	}

	public String getBattutaDelCapo() {
		return battutaDelCapo;
	}

	public String getRisposta() {
		return risposta;
	}

	/**
	 * Il favore che il compagno chiede.
	 */
	public FavoreRichiesto getFavore() {
		return favore;
	}

	/**
	 * Che cosa dice il compagno quando il favore è fatto.
	 */
	public String getRingraziamento() {
		return ringraziamento;
	}

	/**
	 * Il modificatore permanente che il compagno riceve, con la nota {@link #NOTA}.
	 */
	public ModificatoreAttributo nuovoModificatore() {
		return modificatore.nuovo(NOTA);
	}

	/**
	 * Che cosa si scrive quando il compagno riceve il modificatore, con il suo nome al posto di %PERSONAGGIO%.
	 */
	public String getLeale() {
		return leale;
	}
}
