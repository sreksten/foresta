package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.tools.Misc;

public class Troll extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DAL; }

	public String getNomeSingolare() { return "Troll"; }
	public String getNomePlurale() { return "Troll"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Troll() {
		super(ClassiPersonaggio.TROLL);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Troll.gif");
		setForzaMassima(80);
		setMagiaMassima(0);
		setValore(60);
		setCoraggio(80);
		setCarisma(0);
		setQuantitaMassima(3);
		setCorrompibile(true);
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.TERRA;
	}
}
