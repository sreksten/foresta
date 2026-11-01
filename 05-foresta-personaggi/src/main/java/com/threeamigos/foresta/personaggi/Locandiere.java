package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il locandiere: un personaggio non combattente (vedi PersonaggioNonCombattente).
 */
public class Locandiere extends PersonaggioNonCombattente {

	public Locandiere(int livello) {
		this(null, livello);
	}

	public Locandiere(String nome, int livello) {
		super(nome, TipoPersonaggio.LOCANDIERE, livello);
	}
}
