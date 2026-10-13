package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.strumenti.Misc;
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

	@Override
	public String getNomeSingolare() { return "Locandiere"; }

	@Override
	public String getNomePlurale() { return "Locandieri"; }

	@Override public String getAIS() { return Misc.UN; }
	@Override public String getADS() { return Misc.IL; }
	@Override public String getAIP() { return Misc.ALCUNI; }
	@Override public String getADP() { return Misc.I; }
	@Override public String getDeS() { return Misc.DEL; }
	@Override public String getDeP() { return Misc.DEI; }
}
