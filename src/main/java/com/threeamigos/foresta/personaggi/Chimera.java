package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tools.Misc;

public class Chimera extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNA; }
	public String getADS() { return Misc.LA; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELLA; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UNA; }

	public String getNomeSingolare() { return "Chimera"; }
	public String getNomePlurale() { return "Chimere"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoForza() { return 5; }

	public Chimera() {
		super(ClassiPersonaggio.CHIMERA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Chimera.gif");
		setForzaMassima(45);
		setMagiaMassima(0);
		setValore(50);
		setCoraggio(60);
		setCarisma(0);
		setQuantitaMassima(3);
	}
}
