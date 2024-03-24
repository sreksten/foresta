package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.tools.Misc;

public class Arpia extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN_APOSTROFO; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UN_APOSTROFO; }

	public String getNomeSingolare() { return "Arpia"; }
	public String getNomePlurale() { return "Arpie"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoForza() { return 5; }

	public Arpia() {
		super(ClassiPersonaggio.ARPIA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Arpia.gif");
		setForzaMassima(30);
		setMagiaMassima(5);
		setValore(30);
		setCoraggio(30);
		setCarisma(0);
		setQuantitaMassima(4);
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.ARIA;
	}
}
