package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

/**
 * Un bastone magico del bottino: un oggetto che diventa un artefatto di tipo bastone magico (vedi {@link OggettoArtefatto}).
 */
public class BastoneMagico extends OggettoArtefatto {

	public BastoneMagico() {
		super(TipoArtefatto.BASTONE_MAGICO);
	}

	public String getAIS() {
		return Articoli.UN;
	}

	public String getAIP() {
		return Articoli.ALCUNI;
	}

	public String getADS() {
		return Articoli.IL;
	}

	public String getADP() {
		return Articoli.I;
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
