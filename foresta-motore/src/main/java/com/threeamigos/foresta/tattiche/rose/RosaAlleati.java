package com.threeamigos.foresta.tattiche.rose;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.threeamigos.foresta.tattiche.ContestoTattico;

/**
 * I compagni vivi e in campo dell'attore, lui compreso.
 */
final class RosaAlleati implements Rosa {

	@Override
	public TipoRosa getTipo() {
		return TipoRosa.ALLEATI;
	}

	@Override
	public List<Personaggio> candidati(ContestoTattico contesto) {
		return Collections.unmodifiableList(Rosa.inCampo(contesto.getAlleati()));
	}
}
