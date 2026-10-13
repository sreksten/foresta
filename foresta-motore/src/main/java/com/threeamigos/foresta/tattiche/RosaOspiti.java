package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gli ospiti vulnerabili vivi del gruppo del giocatore: da proteggere per i miei personaggi, bersagli per i mostri.
 */
final class RosaOspiti implements Rosa {

	@Override
	public TipoRosa getTipo() {
		return TipoRosa.OSPITI;
	}

	@Override
	public List<Personaggio> candidati(ContestoTattico contesto) {
		return Collections.unmodifiableList(Rosa.inCampo(contesto.getOspitiVulnerabili()));
	}
}
