package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.tipi.TipoArtefatto;
import com.threeamigos.foresta.tools.Misc;

/**
 * Loot a sé, come spada e scudo.
 * (vedi ClassiOggetto.ELMO e l'elenco degli oggetti del Bosco).
 */
public class Elmo extends OggettoArtefatto {

	public Elmo() {
		super(TipoArtefatto.ELMO);
	}

	public String getAIS() {
		return Misc.UN;
	}

	public String getAIP() {
		return Misc.ALCUNI;
	}

	public String getADS() {
		return Misc.L_APOSTROFO;
	}

	public String getADP() {
		return Misc.GLI;
	}

	public String getNomeSingolare() {
		return "elmo";
	}

	public String getNomePlurale() {
		return "elmi";
	}

	public ClassiOggetto getClasse() {
		return ClassiOggetto.ELMO;
	}
}
