package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.mappa.TipoLocazione;

/**
 * La bottega di un alchimista nella Foresta: incantesimi, pozioni, mappe e aumenti di magia, come in città.
 */
public class BottegaDellAlchimista extends Bottega {

	public BottegaDellAlchimista() {
		super(Comando.NEGOZIO_ALCHIMISTA);
	}

	@Override
	public TipoLocazione getTipoLocazione() {
		return TipoLocazione.ALCHIMISTA;
	}
}
