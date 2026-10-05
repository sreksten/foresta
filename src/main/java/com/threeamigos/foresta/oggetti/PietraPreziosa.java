package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tools.Misc;

public class PietraPreziosa extends OggettoBase implements Oggetto {

	public PietraPreziosa() {
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
		return "pietra preziosa";
	}

	public String getNomePlurale() {
		return "pietre preziose";
	}

	public ClassiOggetto getClasse() {
		return ClassiOggetto.PIETRA_PREZIOSA;
	}

	@Override
	public boolean prendi(GruppoGiocatore gruppo, Comando azione) {
		gruppo.addPreziosi(quantita);
		return super.prendi(gruppo, azione);
	}
}
