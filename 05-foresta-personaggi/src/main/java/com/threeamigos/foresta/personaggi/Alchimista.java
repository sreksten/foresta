package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * L'alchimista: un personaggio non combattente (vedi PersonaggioNonCombattente).
 */
public class Alchimista extends PersonaggioNonCombattente {

	public Alchimista(int livello) {
		this(null, livello);
	}

	public Alchimista(String nome, int livello) {
		super(nome, TipoPersonaggio.ALCHIMISTA, livello);
	}
}
