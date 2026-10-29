package com.threeamigos.foresta.tattiche.rose;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tattiche.ContestoTattico;
import com.threeamigos.foresta.tipi.TipoRosa;

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
