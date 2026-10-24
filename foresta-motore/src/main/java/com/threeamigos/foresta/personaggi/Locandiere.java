package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.testi.Articoli;
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

	@Override public String getAIS() { return Articoli.UN; }
	@Override public String getADS() { return Articoli.IL; }
	@Override public String getAIP() { return Articoli.ALCUNI; }
	@Override public String getADP() { return Articoli.I; }
	@Override public String getDeS() { return Articoli.DEL; }
	@Override public String getDeP() { return Articoli.DEI; }
}
