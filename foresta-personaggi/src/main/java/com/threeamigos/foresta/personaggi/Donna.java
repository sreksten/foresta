package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.testi.Articoli;
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
