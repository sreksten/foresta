package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.strumenti.Misc;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

/**
 * Una donna senza una classe propria (la levatrice, le mandanti qualunque): un personaggio non combattente (vedi
 * PersonaggioNonCombattente) con l'immagine e il verso della moglie del bardo.
 */
public class Donna extends PersonaggioNonCombattente {

	public Donna(int livello) {
		this(null, livello);
	}

	public Donna(String nome, int livello) {
		super(nome, TipoPersonaggio.DONNA, livello);
	}

	@Override
	public String getNomeSingolare() { return "Donna"; }

	@Override
	public String getNomePlurale() { return "Donne"; }

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
