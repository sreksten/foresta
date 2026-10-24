package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

/**
 * Una spada del bottino: un oggetto che diventa un artefatto di tipo spada (vedi {@link OggettoArtefatto}).
 */
public class Spada extends OggettoArtefatto {

	public Spada() {
		super(TipoArtefatto.SPADA);
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
		return "spada";
	}

	public String getNomePlurale() {
		return "spade";
	}

	public TipoOggetto getClasse() {
		return TipoOggetto.SPADA;
	}
}
