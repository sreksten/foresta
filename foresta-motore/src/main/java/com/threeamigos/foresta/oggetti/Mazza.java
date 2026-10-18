package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.strumenti.Misc;

/**
 * Una mazza del bottino: un oggetto che diventa un artefatto di tipo mazza (vedi {@link OggettoArtefatto}).
 */
public class Mazza extends OggettoArtefatto {

	public Mazza() {
		super(TipoArtefatto.MAZZA);
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
		return "mazza";
	}

	public String getNomePlurale() {
		return "mazze";
	}

	public TipoOggetto getClasse() {
		return TipoOggetto.MAZZA;
	}
}
