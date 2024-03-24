package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tools.Misc;

public class Bardo extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN; }
	public String getADS() { return Misc.IL; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.I; }
	public String getDeS() { return Misc.DEL; }
	public String getDeP() { return Misc.DEI; }
	public String getDa() { return Misc.DA_UN; }

	public String getNomeSingolare() { return "Bardo"; }
	public String getNomePlurale() { return "Bardi"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Bardo() {
		super(ClassiPersonaggio.BARDO);
	}

	public Bardo(String nome) {
		super(nome, ClassiPersonaggio.BARDO);
	}
	
	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Bardo.gif");
		setIcona("icone/Bardo.gif");
		setForzaMassima(400);
		setMagiaMassima(50);
		setValore(50);
		setCoraggio(50);
		setCarisma(8);
		setCorrompibile(true);
		setAmichevole(true);
	}
}
