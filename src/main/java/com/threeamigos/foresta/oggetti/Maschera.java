package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.tipi.TipoArtefatto;
import com.threeamigos.foresta.tools.Misc;

/**
 * Loot a sé, come spada e scudo.
 * (vedi ClassiOggetto.MASCHERA e l'elenco degli oggetti del Bosco).
 */
public class Maschera extends OggettoArtefatto {

	public Maschera() {
		super(TipoArtefatto.MASCHERA);
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
		return "maschera";
	}

	public String getNomePlurale() {
		return "maschere";
	}

	public ClassiOggetto getClasse() {
		return ClassiOggetto.MASCHERA;
	}
}
