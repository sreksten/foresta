package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.ControlloreDiGioco;

/**
 * Il temporizzatore serve per far ricevere Azione.TIMER ad un ControlloreDiGioco
 * ogni tot secondi, per i combattimenti, la intro, la fine, eccetera.
 */

public interface Temporizzatore {

	void setConsumatore(ControlloreDiGioco controlloreDiGioco);

	void inizia(int secondi);

	void termina();
}