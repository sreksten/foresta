package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.ClassiLocazione;

/**
 * L'armaiolo di Nyena racconta la leggenda di un artefatto leggendario (vedi RecuperaUnArtefattoLeggendario).
 */
public class LaLeggendaDiNyena extends RecuperaUnArtefattoLeggendario {

	public LaLeggendaDiNyena() {
		super(ClasseMissione.LA_LEGGENDA_DI_NYENA);
	}

	@Override
	protected ClassiLocazione getCitta() {
		return ClassiLocazione.CITTA_NYENA;
	}
}
