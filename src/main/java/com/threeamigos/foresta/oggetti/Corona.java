package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.tools.Misc;

public class Corona extends OggettoBase implements Oggetto {

	public Corona() {
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
		return "corona";
	}

	public String getNomePlurale() {
		return "corone";
	}

	public ClassiOggetto getClasse() {
		return ClassiOggetto.CORONA;
	}

	public boolean prendi(GruppoGiocatore gruppo, Comando azione) {
		gruppo.addPreziosi(quantita);
		return super.prendi(gruppo, azione);
	}
}
