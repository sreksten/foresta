package com.threeamigos.foresta.ui.sprite;

/**
 * Un annuncio a tutto schermo (vedi {@link DescrizioneAnnuncioGlobale}) come lo vede lo schermo del gioco: uno alla volta, in coda.
 */
public interface AnnuncioDelloSchermo extends SpriteDelloSchermo {

	/**
	 * Vero per gli annunci che si ripetono a raffica (un punto abilità speso dopo l'altro): il nuovo scarta quelli dello stesso
	 * genere ancora in coda o in corso.
	 */
	boolean isSostituibile();
}
