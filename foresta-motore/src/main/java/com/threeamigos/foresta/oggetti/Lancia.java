package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

/**
 * Una lancia del bottino: un oggetto che diventa un artefatto di tipo lancia (vedi {@link OggettoArtefatto}).
 */
public class Lancia extends OggettoArtefatto {

	public Lancia() {
		super(TipoArtefatto.LANCIA);
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
		return "lancia";
	}

	public String getNomePlurale() {
		return "lance";
	}

	public TipoOggetto getClasse() {
		return TipoOggetto.LANCIA;
	}
}
