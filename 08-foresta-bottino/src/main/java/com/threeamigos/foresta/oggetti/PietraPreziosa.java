package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoBottino;

/**
 * Alcune pietre preziose: prese, si aggiungono ai preziosi del gruppo.
 */
public class PietraPreziosa extends OggettoBase implements Bottino {

	public PietraPreziosa() {
		super();
	}

	public TipoBottino getClasse() {
		return TipoBottino.PIETRA_PREZIOSA;
	}

}
