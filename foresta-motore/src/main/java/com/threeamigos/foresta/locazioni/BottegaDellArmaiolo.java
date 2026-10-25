package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.mappa.TipoLocazione;

/**
 * La bottega di un armaiolo nella Foresta: armi, armature e il resto dell'equipaggiamento, con un magazzino suo.
 */
public class BottegaDellArmaiolo extends Bottega {

	public BottegaDellArmaiolo() {
		super(Comando.NEGOZIO_ARMAIOLO);
	}

	@Override
	public TipoLocazione getTipoLocazione() {
		return TipoLocazione.ARMAIOLO;
	}
}
