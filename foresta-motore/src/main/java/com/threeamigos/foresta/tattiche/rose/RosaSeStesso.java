package com.threeamigos.foresta.tattiche.rose;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.threeamigos.foresta.tattiche.ContestoTattico;

/**
 * Il solo attore: per le azioni su di sé (bere una pozione) e per i controlli sulle proprie condizioni.
 */
final class RosaSeStesso implements Rosa {

	@Override
	public TipoRosa getTipo() {
		return TipoRosa.SE_STESSO;
	}

	@Override
	public List<Personaggio> candidati(ContestoTattico contesto) {
		return Collections.singletonList(contesto.getAttore());
	}
}
