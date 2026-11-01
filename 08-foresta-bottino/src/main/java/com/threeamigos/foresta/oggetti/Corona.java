package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoBottino;

/**
 * Alcune corone: per il gruppo valgono come le pietre preziose e si sommano ai preziosi.
 */
public class Corona extends OggettoBase implements Bottino {

	public Corona() {
		super();
	}

	public TipoBottino getClasse() {
		return TipoBottino.CORONA;
	}

}
