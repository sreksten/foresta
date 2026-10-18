package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.strumenti.Misc;

/**
 * Una lancia del bottino: un oggetto che diventa un artefatto di tipo lancia (vedi {@link OggettoArtefatto}).
 */
public class Lancia extends OggettoArtefatto {

	public Lancia() {
		super(TipoArtefatto.LANCIA);
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
		return "lancia";
	}

	public String getNomePlurale() {
		return "lance";
	}

	public TipoOggetto getClasse() {
		return TipoOggetto.LANCIA;
	}
}
