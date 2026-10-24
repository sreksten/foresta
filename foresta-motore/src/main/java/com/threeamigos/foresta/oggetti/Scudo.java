package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

/**
 * Uno scudo del bottino: un oggetto che diventa un artefatto di tipo scudo (vedi {@link OggettoArtefatto}).
 */
public class Scudo extends OggettoArtefatto {

	public Scudo() {
		super(TipoArtefatto.SCUDO);
	}

	public String getAIS() {
		return Articoli.UNO;
	}

	public String getAIP() {
		return Articoli.ALCUNI;
	}

	public String getADS() {
		return Articoli.LO;
	}

	public String getADP() {
		return Articoli.GLI;
	}

	public String getNomeSingolare() {
		return "scudo";
	}

	public String getNomePlurale() {
		return "scudi";
	}

	public TipoOggetto getClasse() {
		return TipoOggetto.SCUDO;
	}
}
