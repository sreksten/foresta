package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.strumenti.Misc;
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

	@Override public String getAIS() { return Misc.UN; }
	@Override public String getADS() { return Misc.L_APOSTROFO; }
	@Override public String getAIP() { return Misc.ALCUNI; }
	@Override public String getADP() { return Misc.GLI; }
	@Override public String getDeS() { return Misc.DELL_APOSTROFO; }
	@Override public String getDeP() { return Misc.DEGLI; }
}
