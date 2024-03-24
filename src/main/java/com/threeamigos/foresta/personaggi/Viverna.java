package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.tools.Misc;

public class Viverna extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Viverna"; }
	public String getNomePlurale() { return "Viverne"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoMagia() { return 3; }

	public Viverna() {
		super(ClassiPersonaggio.VIVERNA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Viverna.gif");
		setForzaMassima(120);
		setMagiaMassima(50);
		setValore(80);
		setCoraggio(90);
		setCarisma(0);
		setQuantitaMassima(3);
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.FUOCO;
	}
}
