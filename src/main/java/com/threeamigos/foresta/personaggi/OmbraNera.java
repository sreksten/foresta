package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.tools.Misc;

public class OmbraNera extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UN_APOSTROFO; }
	public String getADS() { return Misc.L_APOSTROFO; }
	public String getAIP() { return Misc.ALCUNE; }
	public String getADP() { return Misc.LE; }
	public String getDeS() { return Misc.DELL_APOSTROFO; }
	public String getDeP() { return Misc.DELLE; }
	public String getDa() { return Misc.DA_UN_APOSTROFO; }

	public String getNomeSingolare() { return "Ombra Nera"; }
	public String getNomePlurale() { return "Ombre Nere"; }
	public String getPronome() { return Misc.ESSA; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.FEMMINA; }
	public int getRecuperoMagia() { return 2; }

	public OmbraNera() {
		super(ClassiPersonaggio.OMBRA_NERA);
	}
	
	@Override
	protected void impostaValori() {
		setImmagine("personaggi/OmbraNera.gif");
		setForzaMassima(100);
		setMagiaMassima(50);
		setValore(70);
		setCoraggio(90);
		setCarisma(0);
		setQuantitaMassima(2);
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.MORTE;
	}
}
