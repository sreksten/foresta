package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tools.Misc;

public class Cantastorie extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Cantastorie"; }
	public String getNomePlurale() { return "Cantastorie"; }
	public String getPronome() { return Misc.ELLA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }

	public Cantastorie() {
		super(ClassiPersonaggio.CANTASTORIE);
	}

	public Cantastorie(String nome) {
		super(nome, ClassiPersonaggio.CANTASTORIE);
	}
	
	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Cantastorie.gif");
		setIcona("icone/Cantastorie.gif");
		setForzaMassima(400);
		setMagiaMassima(50);
		setValore(50);
		setCoraggio(50);
		setCarisma(8);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
