package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tools.Misc;

public class Ladra extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Ladra"; }
	public String getNomePlurale() { return "Ladre"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Ladra() {
		super(ClassiPersonaggio.LADRA);
	}

	public Ladra(String nome) {
		super(nome, ClassiPersonaggio.LADRA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Ladra.gif");
		setIcona("icone/Ladra.gif");
		setForzaMassima(450);
		setMagiaMassima(40);
		setValore(60);
		setCoraggio(60);
		setCarisma(6);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
