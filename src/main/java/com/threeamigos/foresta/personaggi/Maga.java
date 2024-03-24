package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tools.Misc;

public class Maga extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Maga"; }
	public String getNomePlurale() { return "Maghe"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoForza() { return 35; }
	public int getRecuperoMagia() { return 3; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * 3; }
	public int getBersagliPerIncantesimo() { return 2 + super.getBersagliPerIncantesimo(); }

	public Maga() {
		super(ClassiPersonaggio.MAGA);
	}

	public Maga(String nome) {
		super(nome, ClassiPersonaggio.MAGA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Maga.gif");
		setIcona("icone/Maga.gif");
		setForzaMassima(350);
		setMagiaMassima(70);
		setValore(30);
		setCoraggio(30);
		setCarisma(6);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
