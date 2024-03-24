package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tools.Misc;

public class Spada extends OggettoBase implements Oggetto {

	public Spada() {
		super();
	}

	public String getAIS() {
		return Misc.UNA;
	}

	public String getAIP() {
		return Misc.ALCUNE;
	}

	public String getADS() {
		return Misc.LA;
	}

	public String getADP() {
		return Misc.LE;
	}

	public String getNomeSingolare() {
		return "spada";
	}

	public String getNomePlurale() {
		return "spade";
	}

	public ClassiOggetto getClasse() {
		return ClassiOggetto.SPADA;
	}
}
