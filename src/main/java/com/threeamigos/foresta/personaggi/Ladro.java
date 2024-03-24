package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tools.Misc;

public class Ladro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Ladro"; }
	public String getNomePlurale() { return "Ladri"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Ladro() {
		super(ClassiPersonaggio.LADRO);
	}

	public Ladro(String nome) {
		super(nome, ClassiPersonaggio.LADRO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Ladro.gif");
		setIcona("icone/Ladro.gif");
		setForzaMassima(450);
		setMagiaMassima(40);
		setValore(60);
		setCoraggio(60);
		setCarisma(6);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
