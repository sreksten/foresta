package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * L'incantatore: un personaggio non combattente (vedi PersonaggioNonCombattente).
 */
public class Incantatore extends PersonaggioNonCombattente {

	public Incantatore(int livello) {
		this(null, livello);
	}

	public Incantatore(String nome, int livello) {
		super(nome, TipoPersonaggio.INCANTATORE, livello);
	}
}
