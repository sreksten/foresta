package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * I vivi e in campo dell'altro gruppo.
 */
final class RosaAvversari implements Rosa {

	@Override
	public TipoRosa getTipo() {
		return TipoRosa.AVVERSARI;
	}

	@Override
	public List<Personaggio> candidati(ContestoTattico contesto) {
		return Collections.unmodifiableList(Rosa.inCampo(contesto.getAvversari()));
	}
}
