package com.threeamigos.foresta.ui.sprite;

/**
 * Uno sprite come lo vede lo schermo del gioco (<code>SchermoDelGioco</code>), senza toolkit: lo tiene finché è attivo. Lo
 * implementano gli sprite di Swing e quelli di libGDX.
 */
public interface SpriteDelloSchermo {

	/**
	 * Falso quando lo sprite ha finito: lo schermo lo toglie dopo averlo disegnato.
	 */
	boolean isAttivo();
}
