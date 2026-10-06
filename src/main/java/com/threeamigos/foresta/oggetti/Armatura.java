package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.strumenti.Misc;

/**
 * Loot a sé, come spada e scudo.
 * (vedi TipoOggetto.ARMATURA e l'elenco degli oggetti del Bosco).
 */
public class Armatura extends OggettoArtefatto {

	public Armatura() {
		super(TipoArtefatto.ARMATURA);
	}

	public String getAIS() {
		return Misc.UNA;
	}

	public String getAIP() {
		return Misc.ALCUNE;
	}

	public String getADS() {
		return Misc.L_APOSTROFO;
	}

	public String getADP() {
		return Misc.LE;
	}

	public String getNomeSingolare() {
		return "armatura";
	}

	public String getNomePlurale() {
		return "armature";
	}

	public TipoOggetto getClasse() {
		return TipoOggetto.ARMATURA;
	}
}
