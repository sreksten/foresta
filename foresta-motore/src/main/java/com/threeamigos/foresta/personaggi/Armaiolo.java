package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.strumenti.Misc;
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

	@Override public String getAIS() { return Misc.UN; }
	@Override public String getADS() { return Misc.L_APOSTROFO; }
	@Override public String getAIP() { return Misc.ALCUNI; }
	@Override public String getADP() { return Misc.GLI; }
	@Override public String getDeS() { return Misc.DELL_APOSTROFO; }
	@Override public String getDeP() { return Misc.DEGLI; }
}
