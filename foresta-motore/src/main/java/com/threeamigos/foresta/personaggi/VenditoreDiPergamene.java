package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.strumenti.Misc;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Il venditore di pergamene: un personaggio non combattente (vedi PersonaggioNonCombattente).
 */
public class VenditoreDiPergamene extends PersonaggioNonCombattente {

	public VenditoreDiPergamene(int livello) {
		this(null, livello);
	}

	public VenditoreDiPergamene(String nome, int livello) {
		super(nome, TipoPersonaggio.VENDITORE_DI_PERGAMENE, livello);
	}

	@Override
	public String getNomeSingolare() { return "Venditore di pergamene"; }

	@Override
	public String getNomePlurale() { return "Venditori di pergamene"; }

	@Override public String getAIS() { return Misc.UN; }
	@Override public String getADS() { return Misc.IL; }
	@Override public String getAIP() { return Misc.ALCUNI; }
	@Override public String getADP() { return Misc.I; }
	@Override public String getDeS() { return Misc.DEL; }
	@Override public String getDeP() { return Misc.DEI; }
}
