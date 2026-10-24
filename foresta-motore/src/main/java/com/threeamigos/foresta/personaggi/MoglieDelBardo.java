package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * La moglie del bardo: un personaggio non combattente (vedi PersonaggioNonCombattente).
 */
public class MoglieDelBardo extends PersonaggioNonCombattente {

	public MoglieDelBardo(int livello) {
		this(null, livello);
	}

	public MoglieDelBardo(String nome, int livello) {
		super(nome, TipoPersonaggio.MOGLIE_DEL_BARDO, livello);
	}

	@Override
	public String getNomeSingolare() { return "Moglie del Bardo"; }

	@Override
	public String getNomePlurale() { return "Mogli del Bardo"; }

	@Override public String getAIS() { return Articoli.UNA; }
	@Override public String getADS() { return Articoli.LA; }
	@Override public String getAIP() { return Articoli.ALCUNE; }
	@Override public String getADP() { return Articoli.LE; }
	@Override public String getDeS() { return Articoli.DELLA; }
	@Override public String getDeP() { return Articoli.DELLE; }
	@Override public String getDa() { return Articoli.DA_UNA; }
	@Override public String getPronome() { return Articoli.ELLA; }
	@Override public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
}
