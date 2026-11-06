package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.ui.Finestra;

/**
 * La schermata delle tattiche come la comanda lo schermo del gioco: la logica è dell'{@link EditorDelleTattiche}, il disegno di Swing
 * e di libGDX.
 */
public interface SchermataTattiche extends Finestra {

	EditorDelleTattiche getEditor();
}
