package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.strumenti.Misc;
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

	@Override public String getAIS() { return Misc.UNA; }
	@Override public String getADS() { return Misc.LA; }
	@Override public String getAIP() { return Misc.ALCUNE; }
	@Override public String getADP() { return Misc.LE; }
	@Override public String getDeS() { return Misc.DELLA; }
	@Override public String getDeP() { return Misc.DELLE; }
	@Override public String getDa() { return Misc.DA_UNA; }
	@Override public String getPronome() { return Misc.ELLA; }
	@Override public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
}
