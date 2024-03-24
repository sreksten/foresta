package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tools.Misc;

public class Guerriero extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Guerriero"; }
	public String getNomePlurale() { return "Guerrieri"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }
	public int getModificaDanniForza() { return 2; }

	public Guerriero() {
		super(ClassiPersonaggio.GUERRIERO);
	}

	public Guerriero(String nome) {
		super(nome, ClassiPersonaggio.GUERRIERO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Guerriero.gif");
		setIcona("icone/Guerriero.gif");
		setForzaMassima(550);
		setMagiaMassima(30);
		setValore(70);
		setCoraggio(70);
		setCarisma(5);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
