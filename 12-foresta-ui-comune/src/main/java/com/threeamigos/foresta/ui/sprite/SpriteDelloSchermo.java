package com.threeamigos.foresta.ui.sprite;

import com.threeamigos.foresta.ui.Area;

/**
 * Uno sprite come lo vede lo schermo del gioco (<code>SchermoDelGioco</code>), senza toolkit: lo tiene finché è attivo. Lo
 * implementano gli sprite di Swing e quelli di libGDX.
 */
public interface SpriteDelloSchermo {

	/**
	 * Falso quando lo sprite ha finito: lo schermo lo toglie dopo averlo disegnato.
	 */
	boolean isAttivo();

	/**
	 * Limita il disegno a quest'area, in coordinate dello schermo (null: nessun limite; vuota: non si vede). Lo schermo la cambia a ogni fotogramma per gli
	 * sprite nati in un riquadro della pila, che si vedono solo dove il riquadro non è coperto dagli altri. Chi non si ritaglia la ignora.
	 */
	default void setRitaglio(Area ritaglio) {
	}
}
