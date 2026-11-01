package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * L'armaiolo: un personaggio non combattente (vedi PersonaggioNonCombattente).
 */
public class Armaiolo extends PersonaggioNonCombattente {

	public Armaiolo(int livello) {
		this(null, livello);
	}

	public Armaiolo(String nome, int livello) {
		super(nome, TipoPersonaggio.ARMAIOLO, livello);
	}
}
