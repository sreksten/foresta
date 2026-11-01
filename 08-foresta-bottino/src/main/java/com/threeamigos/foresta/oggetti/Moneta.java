package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoBottino;

/**
 * Alcune monete d'oro: prese, si aggiungono alle monete del gruppo.
 */
public class Moneta extends OggettoBase implements Bottino {

	public Moneta() {
		super();
	}

	public TipoBottino getClasse() {
		return TipoBottino.MONETA;
	}

}

