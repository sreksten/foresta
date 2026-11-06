package com.threeamigos.foresta.ui.sprite;

/**
 * Un fumetto (vedi {@link DescrizioneFumetto}) come lo vede lo schermo del gioco: uno alla volta, in coda.
 */
public interface FumettoDelloSchermo extends SpriteDelloSchermo {

	String getTesto();

	/**
	 * Fa ripartire il fumetto da capo: lo stesso fumetto chiesto di nuovo non si accoda.
	 */
	void resettaImpulsi();
}
