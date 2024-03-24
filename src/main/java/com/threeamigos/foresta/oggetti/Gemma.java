package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.tools.Misc;

public class Gemma extends OggettoBase implements Oggetto {

	public Gemma() {
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
		return "gemma";
	}

	public String getNomePlurale() {
		return "gemme";
	}

	public ClassiOggetto getClasse() {
		return ClassiOggetto.GEMMA;
	}

	@Override
	public boolean prendi(GruppoGiocatore gruppo, Comando azione) {
		gruppo.addPreziosi(quantita);
		return super.prendi(gruppo, azione);
	}
}
