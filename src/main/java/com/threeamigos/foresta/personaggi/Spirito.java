package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.incantesimi.ClassiIncantesimo;
import com.threeamigos.foresta.tools.Misc;

public class Spirito extends PersonaggioBase implements Personaggio {

	public String getAIS() { return Misc.UNO; }
	public String getADS() { return Misc.LO; }
	public String getAIP() { return Misc.ALCUNI; }
	public String getADP() { return Misc.GLI; }
	public String getDeS() { return Misc.DELLO; }
	public String getDeP() { return Misc.DEGLI; }
	public String getDa() { return Misc.DA_UNO; }

	public String getNomeSingolare() { return "Spirito"; }
	public String getNomePlurale() { return "Spiriti"; }
	public String getPronome() { return Misc.ESSO; }
	public Personaggio.Sesso getSesso() { return Personaggio.Sesso.MASCHIO; }

	public Spirito() {
		super(ClassiPersonaggio.SPIRITO);
	}

	@Override
	protected void impostaValori() {
		setImmagine("personaggi/Spirito.gif");
		setForzaMassima(40);
		setMagiaMassima(0);
		setValore(50);
		setCoraggio(50);
		setCarisma(0);
		setQuantitaMassima(2);
		setAmichevole(true);
	}

	@Override
	public boolean isImmuneAIncantesimo(ClassiIncantesimo classeIncantesimo) {
		return classeIncantesimo == ClassiIncantesimo.MORTE;
	}
}
