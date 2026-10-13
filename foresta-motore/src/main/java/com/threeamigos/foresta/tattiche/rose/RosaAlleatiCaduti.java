package com.threeamigos.foresta.tattiche.rose;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.threeamigos.foresta.tattiche.ContestoTattico;

/**
 * I compagni caduti, per la Resurrezione. Chi è in panchina non è caduto: è vivo, solo fuori dallo scontro.
 */
final class RosaAlleatiCaduti implements Rosa {

	@Override
	public TipoRosa getTipo() {
		return TipoRosa.ALLEATI_CADUTI;
	}

	@Override
	public List<Personaggio> candidati(ContestoTattico contesto) {
		List<Personaggio> caduti = new ArrayList<>();
		for (Personaggio alleato : contesto.getAlleati()) {
			if (!alleato.isVivo() && !alleato.isInPanchina() && alleato.isCurabile()) {
				caduti.add(alleato);
			}
		}
		return Collections.unmodifiableList(caduti);
	}
}
