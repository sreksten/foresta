package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.mappa.TipoLocazione;

/**
 * Il negozio di un venditore di pergamene nella Foresta: ingredienti magici, con un magazzino suo.
 */
public class BottegaDelVenditoreDiPergamene extends Bottega {

	public BottegaDelVenditoreDiPergamene() {
		super(Comando.NEGOZIO_VENDITORE_DI_PERGAMENE);
	}

	@Override
	public TipoLocazione getTipoLocazione() {
		return TipoLocazione.VENDITORE_DI_PERGAMENE;
	}
}
