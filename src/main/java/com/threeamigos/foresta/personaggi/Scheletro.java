package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.tools.Misc;

public class Scheletro extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNO; }
	public String getADS() { return Misc.LO; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.GLI; }
	public String getDeS() { return Misc.DELLO; }
	public String getDeP() { return Misc.DEGLI; }
	public String getDa() { return Misc.DA_UNO; }

	public String getNomeSingolare() { return "Scheletro"; }
	public String getNomePlurale() { return "Scheletri"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Scheletro() {
		super(ClassiPersonaggio.SCHELETRO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Scheletro.gif");
		setForzaMassima(50);
		setMagiaMassima(0);
		setValore(40);
		setCoraggio(50);
		setCarisma(0);
		setQuantitaMassima(4);
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.MORTE;
	}
}
