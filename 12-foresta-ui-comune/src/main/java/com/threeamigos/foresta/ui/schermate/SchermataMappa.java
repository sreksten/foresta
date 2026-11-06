package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.ui.Finestra;

/**
 * La mappa a tutto schermo come la comanda lo schermo del gioco: la logica è del {@link MappaATuttoSchermo}, il disegno di Swing e di
 * libGDX.
 */
public interface SchermataMappa extends Finestra {

	MappaATuttoSchermo getModello();
}
