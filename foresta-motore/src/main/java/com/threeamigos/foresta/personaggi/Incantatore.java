package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.testi.Articoli;
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

	@Override
	public String getNomeSingolare() { return "Incantatore"; }

	@Override
	public String getNomePlurale() { return "Incantatori"; }

	@Override public String getAIS() { return Articoli.UN; }
	@Override public String getADS() { return Articoli.L_APOSTROFO; }
	@Override public String getAIP() { return Articoli.ALCUNI; }
	@Override public String getADP() { return Articoli.GLI; }
	@Override public String getDeS() { return Articoli.DELL_APOSTROFO; }
	@Override public String getDeP() { return Articoli.DEGLI; }
}
