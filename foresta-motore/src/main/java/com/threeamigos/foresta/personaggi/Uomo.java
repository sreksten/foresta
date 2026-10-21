package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Un uomo senza una classe propria (i mandanti qualunque): un personaggio non combattente (vedi
 * PersonaggioNonCombattente) con l'immagine e il verso del locandiere.
 */
public class Uomo extends PersonaggioNonCombattente {

	public Uomo(int livello) {
		this(null, livello);
	}

	public Uomo(String nome, int livello) {
		super(nome, TipoPersonaggio.UOMO, livello);
	}

	@Override
	public String getNomeSingolare() { return "Uomo"; }

	@Override
	public String getNomePlurale() { return "Uomini"; }
}
