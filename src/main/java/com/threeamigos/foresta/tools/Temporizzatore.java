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

	/**
	 * Avvia gli impulsi: il primo subito, poi uno ogni millisecondi. Adatto a ciò che
	 * deve partire immediatamente, come il primo round di un combattimento.
	 */
	void inizia(int millisecondi);

	/**
	 * Avvia gli impulsi: il primo dopo millisecondi, poi uno ogni millisecondi. Adatto
	 * alle sequenze di schermate, dove la prima deve restare visibile per un periodo intero.
	 */
	void iniziaDopo(int millisecondi);

	void termina();
}