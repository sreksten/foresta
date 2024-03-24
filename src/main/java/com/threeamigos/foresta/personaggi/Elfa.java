package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tools.Misc;

public class Elfa extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN_APOSTROFO; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UN_APOSTROFO; }

	public String getNomeSingolare() { return "Elfa"; }
	public String getNomePlurale() { return "Elfe"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoMagia() { return 2; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * 2; }
	public int getBersagliPerIncantesimo() { return 1 + super.getBersagliPerIncantesimo(); }

	public Elfa() {
		super(ClassiPersonaggio.ELFA);
	}

	public Elfa(String nome) {
		super(nome, ClassiPersonaggio.ELFA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Elfa.gif");
		setIcona("icone/Elfa.gif");
		setForzaMassima(350);
		setMagiaMassima(60);
		setValore(40);
		setCoraggio(40);
		setCarisma(7);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
