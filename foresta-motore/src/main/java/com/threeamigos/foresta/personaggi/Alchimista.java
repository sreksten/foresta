package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.strumenti.Misc;
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

	@Override
	public String getNomeSingolare() { return "Alchimista"; }

	@Override
	public String getNomePlurale() { return "Alchimisti"; }

	@Override public String getAIS() { return Misc.UN; }
	@Override public String getADS() { return Misc.L_APOSTROFO; }
	@Override public String getAIP() { return Misc.ALCUNI; }
	@Override public String getADP() { return Misc.GLI; }
	@Override public String getDeS() { return Misc.DELL_APOSTROFO; }
	@Override public String getDeP() { return Misc.DEGLI; }
}
