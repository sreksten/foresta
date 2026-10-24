package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

/**
 * Una mazza del bottino: un oggetto che diventa un artefatto di tipo mazza (vedi {@link OggettoArtefatto}).
 */
public class Mazza extends OggettoArtefatto {

	public Mazza() {
		super(TipoArtefatto.MAZZA);
	}

	public String getAIS() {
		return Articoli.UNA;
	}

	public String getAIP() {
		return Articoli.ALCUNE;
	}

	public String getADS() {
		return Articoli.LA;
	}

	public String getADP() {
		return Articoli.LE;
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
