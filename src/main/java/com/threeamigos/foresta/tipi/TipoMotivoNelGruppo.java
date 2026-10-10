package com.threeamigos.foresta.tipi;

/**
 * Perché un personaggio si trova nel gruppo del giocatore.
 */
public enum TipoMotivoNelGruppo {

	/**
	 * Ne fa parte: il giocatore, o chi si è unito per restare (a una locanda, per esempio).
	 */
	PERMANENTE(false, false),
	/**
	 * Un PNG amichevole che si è offerto di accompagnare il gruppo per qualche turno (vedi AiutoGratuito).
	 */
	AIUTO_GRATUITO(true, false),
	/**
	 * Un PNG che il gruppo ha pagato perché lo accompagni per qualche turno (vedi AiutoMercenario).
	 */
	AIUTO_MERCENARIO(true, false),
	/**
	 * Lo Scheletro nato dalla MIETITURA di un minion: accompagna il gruppo per qualche turno come un aiuto, ma se muore
	 * se ne va subito, senza aspettare di essere risuscitato.
	 */
	MIETITURA(true, true);

	private final boolean aTempo;
	private final boolean lasciaSeMuore;

	TipoMotivoNelGruppo(boolean aTempo, boolean lasciaSeMuore) {
		this.aTempo = aTempo;
		this.lasciaSeMuore = lasciaSeMuore;
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
