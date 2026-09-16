package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.Temporizzabile;

/**
 * Il temporizzatore serve per far ricevere ad un oggetto un "tick"
 * ogni tot tempo, per i combattimenti, la intro, la fine, eccetera.
 */

public interface Temporizzatore {

	void setTemporizzabile(Temporizzabile temporizzabile);

	void inizia(int millisecondi);

	void termina();
}