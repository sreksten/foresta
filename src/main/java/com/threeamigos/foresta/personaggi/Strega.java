package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.tools.Misc;

public class Strega extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Strega"; }
	public String getNomePlurale() { return "Streghe"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoMagia() { return 35; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * 3; }
	public int getBersagliPerIncantesimo() { return 5; }

	public Strega() {
		super(ClassiPersonaggio.STREGA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Strega.gif");
		setForzaMassima(800);
		setMagiaMassima(350);
		setValore(50);
		setCoraggio(60);
		setCarisma(0);
	}

	@Override
	public boolean isParteConValoriMassimi() {
		return true;
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.ACQUA ||
				classeIncantesimo == ClassiIncantesimo.ARIA ||
				classeIncantesimo == ClassiIncantesimo.FUOCO ||
				classeIncantesimo == ClassiIncantesimo.TERRA;
	}
}
