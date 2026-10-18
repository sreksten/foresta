package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.strumenti.Misc;

/**
 * Un bastone magico del bottino: un oggetto che diventa un artefatto di tipo bastone magico (vedi {@link OggettoArtefatto}).
 */
public class BastoneMagico extends OggettoArtefatto {

	public BastoneMagico() {
		super(TipoArtefatto.BASTONE_MAGICO);
	}

	public String getAIS() {
		return Misc.UN;
	}

	public String getAIP() {
		return Misc.ALCUNI;
	}

	public String getADS() {
		return Misc.IL;
	}

	public String getADP() {
		return Misc.I;
	}

	public String getNomeSingolare() {
		return "bastone magico";
	}

	public String getNomePlurale() {
		return "bastoni magici";
	}

	public TipoOggetto getClasse() {
		return TipoOggetto.BASTONE_MAGICO;
	}
}
