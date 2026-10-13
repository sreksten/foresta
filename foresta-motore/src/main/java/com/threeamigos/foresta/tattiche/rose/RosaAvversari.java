package com.threeamigos.foresta.tattiche.rose;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.threeamigos.foresta.tattiche.ContestoTattico;

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
