package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.testi.Articoli;
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

	@Override public String getAIS() { return Articoli.UN; }
	@Override public String getADS() { return Articoli.L_APOSTROFO; }
	@Override public String getAIP() { return Articoli.ALCUNI; }
	@Override public String getADP() { return Articoli.GLI; }
	@Override public String getDeS() { return Articoli.DELL_APOSTROFO; }
	@Override public String getDeP() { return Articoli.DEGLI; }
}
