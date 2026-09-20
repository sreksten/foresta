package com.threeamigos.foresta.tools;

import com.threeamigos.foresta.motore.Temporizzabile;

/**
 * Il temporizzatore serve per far ricevere ad un oggetto un impulso
 * ogni tot tempo, per i combattimenti, la intro, la fine, le animazioni, eccetera.
 */

public interface Temporizzatore {

	int FRAME_PER_SECONDO = 30;
	int DURATA_FRAME_IN_MILLISECONDI = 1000 / FRAME_PER_SECONDO;

	void setTemporizzabile(Temporizzabile temporizzabile);

	void inizia(int millisecondi);

	void termina();
}