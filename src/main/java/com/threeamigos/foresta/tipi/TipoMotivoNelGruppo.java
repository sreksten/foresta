package com.threeamigos.foresta.tipi;

/**
 * Perché un personaggio si trova nel gruppo del giocatore.
 */
public enum TipoMotivoNelGruppo {

	/**
	 * Ne fa parte: il giocatore, o chi si è unito per restare (a una locanda, per esempio).
	 */
	PERMANENTE(false, false, true),
	/**
	 * Un PNG amichevole che si è offerto di accompagnare il gruppo per qualche turno (vedi AiutoGratuito).
	 */
	AIUTO_GRATUITO(true, false, true),
	/**
	 * Un PNG che il gruppo ha pagato perché lo accompagni per qualche turno (vedi AiutoMercenario).
	 */
	AIUTO_MERCENARIO(true, false, true),
	/**
	 * Lo Scheletro nato dalla MIETITURA di un minion: accompagna il gruppo per qualche turno come un aiuto, ma se muore
	 * se ne va subito, senza aspettare di essere risuscitato.
	 */
	MIETITURA(true, true, false);

	private final boolean aTempo;
	private final boolean lasciaSeMuore;
	private final boolean entraVolentieri;

	TipoMotivoNelGruppo(boolean aTempo, boolean lasciaSeMuore, boolean entraVolentieri) {
		this.aTempo = aTempo;
		this.lasciaSeMuore = lasciaSeMuore;
		this.entraVolentieri = entraVolentieri;
	}

	/**
	 * Se il personaggio entra nel gruppo per sua scelta (si unisce, si offre di accompagnarlo, accetta un
	 * ingaggio) e quindi, entrando, può dirsi contento. Non lo è chi vi viene portato senza essere stato interpellato,
	 * come lo Scheletro risvegliato dalla MIETITURA: per lui il testo dice soltanto che entra nel gruppo.
	 */
	public boolean isEntraVolentieri() {
		return entraVolentieri;
	}

	/**
	 * Se il personaggio, quando muore, lascia subito il gruppo invece di restarci da morto.
	 */
	public boolean isLasciaSeMuore() {
		return lasciaSeMuore;
	}

	/**
	 * Se il personaggio resta per un numero di turni (il suo TEMPO) e poi se ne va.
	 */
	public boolean isATempo() {
		return aTempo;
	}
}
