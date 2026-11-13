package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * La moglie del bardo: un personaggio non combattente (vedi PersonaggioNonCombattente).
 */
public class MoglieDelBardo extends PersonaggioNonCombattente {

	public MoglieDelBardo(int livello) {
		this(null, livello);
	}

	public MoglieDelBardo(String nome, int livello) {
		super(nome, TipoPersonaggio.MOGLIE_DEL_BARDO, livello);
	}

}
