package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.tools.Misc;

public class OmbraFiamma extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN_APOSTROFO; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DALL_APOSTROFO; }

	public String getNomeSingolare() { return "OmbraFiamma"; }
	public String getNomePlurale() { return "OmbreFiamma"; }
	public String getPronome() { return Misc.EGLI; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoMagia() { return 5; }
	public int getModificaDanniForza() { return 3; }
	public int getModificaDanniMagia(int danniBase) { return danniBase * 5; }
	public int getBersagliPerIncantesimo() { return 4 + super.getBersagliPerIncantesimo(); }

	public OmbraFiamma() {
		super(ClassiPersonaggio.OMBRAFIAMMA);
	}

	public OmbraFiamma(String nome) {
		super(nome, ClassiPersonaggio.OMBRAFIAMMA);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/OmbraFiamma.gif");
		setIcona("icone/OmbraFiamma.gif");
		setForzaMassima(500);
		setMagiaMassima(90);
		setValore(100);
		setCoraggio(100);
		setCarisma(9);
		setCorrompibile(true);
		setAmichevole(true);
	}

	@Override
	public boolean isImmortale() {
		return true;
	}

	@Override
	public int getDanniInCombattimento() {
		return 100;
	}
}
