package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tools.Misc;

public class Elfo extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.GLI; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DEGLI; }
	public String getDa() { return Misc.DALL_APOSTROFO; }

	public String getNomeSingolare() { return "Elfo"; }
	public String getNomePlurale() { return "Elfi"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }
	public int getRecuperoMagia() { return 2; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * 2; }
	public int getBersagliPerIncantesimo() { return 1 + super.getBersagliPerIncantesimo(); }

	public Elfo() {
		super(ClassiPersonaggio.ELFO);
	}

	public Elfo(String nome) {
		super(nome, ClassiPersonaggio.ELFO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Elfo.gif");
		setIcona("icone/Elfo.gif");
		setForzaMassima(350);
		setMagiaMassima(60);
		setValore(40);
		setCoraggio(40);
		setCarisma(7);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
