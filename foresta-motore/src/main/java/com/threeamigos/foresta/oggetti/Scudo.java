package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.strumenti.Misc;

public class Scudo extends OggettoArtefatto {

	public Scudo() {
		super(TipoArtefatto.SCUDO);
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

	public TipoOggetto getClasse() {
		return TipoOggetto.SCUDO;
	}
}
