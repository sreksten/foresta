package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.mappa.TipoLocazione;

/**
 * Il laboratorio di un incantatore nella Foresta: il banco su cui fondere gli artefatti con le pergamene.
 */
public class BottegaDellIncantatore extends Bottega {

	public BottegaDellIncantatore() {
		super(Comando.NEGOZIO_INCANTATORE);
	}

	@Override
	public TipoLocazione getTipoLocazione() {
		return TipoLocazione.INCANTATORE;
	}
}
