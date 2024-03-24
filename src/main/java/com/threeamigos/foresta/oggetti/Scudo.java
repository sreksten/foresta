package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tools.Misc;

public class Scudo extends OggettoBase implements Oggetto {

	public Scudo() {
		super();
	}

	public String getAIS() {
		return Misc.UNO;
	}

	public String getAIP() {
		return Misc.ALCUNI;
	}

	public String getADS() {
		return Misc.LO;
	}

	public String getADP() {
		return Misc.GLI;
	}

	public String getNomeSingolare() {
		return "scudo";
	}

	public String getNomePlurale() {
		return "scudi";
	}

	public ClassiOggetto getClasse() {
		return ClassiOggetto.SCUDO;
	}
}
