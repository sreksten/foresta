package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tools.Misc;

public class Guerriera extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Guerriera"; }
	public String getNomePlurale() { return "Guerriere"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getModificaDanniForza() { return 2; }

	public Guerriera() {
		super(ClassiPersonaggio.GUERRIERA);
	}

	public Guerriera(String nome) {
		super(nome, ClassiPersonaggio.GUERRIERA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Guerriera.gif");
		setIcona("icone/Guerriera.gif");
		setForzaMassima(550);
		setMagiaMassima(30);
		setValore(70);
		setCoraggio(70);
		setCarisma(5);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
