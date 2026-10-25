package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il borgomastro di una città: un personaggio non combattente (vedi PersonaggioNonCombattente) con l'immagine sua.
 */
public class Borgomastro extends PersonaggioNonCombattente {

	public Borgomastro(int livello) {
		this(null, livello);
	}

	public Borgomastro(String nome, int livello) {
		super(nome, TipoPersonaggio.BORGOMASTRO, livello);
	}

	@Override
	public String getNomeSingolare() { return "Borgomastro"; }

	@Override
	public String getNomePlurale() { return "Borgomastri"; }
}
