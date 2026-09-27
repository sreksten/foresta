package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.tipi.TipoArtefatto;
import com.threeamigos.foresta.tools.Misc;

public class Spada extends OggettoArtefatto {

	public Spada() {
		super(TipoArtefatto.SPADA);
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
