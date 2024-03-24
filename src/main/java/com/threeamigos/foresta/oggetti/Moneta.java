package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.tools.Misc;

public class Moneta extends OggettoBase implements Oggetto {

	public Moneta() {
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
		return "moneta";
	}

	public String getNomePlurale() {
		return "monete";
	}

	public ClassiOggetto getClasse() {
		return ClassiOggetto.MONETA;
	}

	public boolean prendi(GruppoGiocatore gruppo, Comando azione) {
		gruppo.addMonete(quantita);
		return super.prendi(gruppo, azione);
	}
}

