package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.List;

/**
 * Un gruppo fatto sul momento con i bersagli scelti di un incantesimo a più bersagli (la tattica sceglie il principale e i successivi): l'incantesimo
 * lo vede come il gruppo avversario e colpisce i suoi vivi. Non è il gruppo di nessuno e non si usa altro che per questo.
 */
public final class GruppoDiBersagli extends Gruppo {

	public GruppoDiBersagli(List<Personaggio> bersagli) {
		personaggi.addAll(bersagli);
		if (!bersagli.isEmpty()) {
			capo = bersagli.get(0);
		}
	}

	@Override
	public boolean isGruppoGiocatore() {
		return false;
	}
}
