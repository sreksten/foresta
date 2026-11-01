package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il venditore di pergamene: un personaggio non combattente (vedi PersonaggioNonCombattente).
 */
public class VenditoreDiPergamene extends PersonaggioNonCombattente {

	public VenditoreDiPergamene(int livello) {
		this(null, livello);
	}

	public VenditoreDiPergamene(String nome, int livello) {
		super(nome, TipoPersonaggio.VENDITORE_DI_PERGAMENE, livello);
	}
}
