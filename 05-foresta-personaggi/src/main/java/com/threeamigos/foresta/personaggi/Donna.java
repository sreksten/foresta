package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Una donna senza una classe propria (la levatrice, le mandanti qualunque): un personaggio non combattente (vedi
 * PersonaggioNonCombattente) con l'immagine e il verso della moglie del bardo.
 */
public class Donna extends PersonaggioNonCombattente {

	public Donna(int livello) {
		this(null, livello);
	}

	public Donna(String nome, int livello) {
		super(nome, TipoPersonaggio.DONNA, livello);
	}

	@Override public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
}
