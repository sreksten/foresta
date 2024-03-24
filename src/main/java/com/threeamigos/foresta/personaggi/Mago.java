package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tools.Misc;

public class Mago extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DAL; }

	public String getNomeSingolare() { return "Mago"; }
	public String getNomePlurale() { return "Maghi"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }
	public int getRecuperoForza() { return 35; }
	public int getRecuperoMagia() { return 3; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * 3; }
	public int getBersagliPerIncantesimo() { return 2 + super.getBersagliPerIncantesimo(); }

	public Mago() {
		super(ClassiPersonaggio.MAGO);
	}

	public Mago(String nome) {
		super(nome, ClassiPersonaggio.MAGO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Mago.gif");
		setIcona("icone/Mago.gif");
		setForzaMassima(350);
		setMagiaMassima(70);
		setValore(30);
		setCoraggio(30);
		setCarisma(6);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
