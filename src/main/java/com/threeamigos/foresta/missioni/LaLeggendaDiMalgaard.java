package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;

/**
 * L'armaiolo di Malgaard racconta la leggenda di un artefatto leggendario (vedi RecuperaUnArtefattoLeggendario).
 */
public class LaLeggendaDiMalgaard extends RecuperaUnArtefattoLeggendario {

	public LaLeggendaDiMalgaard() {
		super(ClasseMissione.LA_LEGGENDA_DI_MALGAARD);
	}

	@Override
	protected ClassiLocazione getCitta() {
		return ClassiLocazione.CITTA_MALGAARD;
	}
}
