package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.strumenti.Misc;

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

	public TipoOggetto getClasse() {
		return TipoOggetto.CORONA;
	}

	public boolean prendi(GruppoGiocatore gruppo, Comando azione) {
		gruppo.addPreziosi(quantita);
		return super.prendi(gruppo, azione);
	}
}
