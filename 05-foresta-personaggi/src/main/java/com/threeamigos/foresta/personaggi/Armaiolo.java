package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.testi.Articoli;
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

	@Override
	public String getNomeSingolare() { return "Armaiolo"; }

	@Override
	public String getNomePlurale() { return "Armaioli"; }

	@Override public String getAIS() { return Articoli.UN; }
	@Override public String getADS() { return Articoli.L_APOSTROFO; }
	@Override public String getAIP() { return Articoli.ALCUNI; }
	@Override public String getADP() { return Articoli.GLI; }
	@Override public String getDeS() { return Articoli.DELL_APOSTROFO; }
	@Override public String getDeP() { return Articoli.DEGLI; }
}
