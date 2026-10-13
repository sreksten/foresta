package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.strumenti.Misc;

/**
 * Loot a sé, come spada e scudo.
 * (vedi TipoOggetto.MASCHERA e l'elenco degli oggetti del Bosco).
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

	public TipoOggetto getClasse() {
		return TipoOggetto.MASCHERA;
	}
}
