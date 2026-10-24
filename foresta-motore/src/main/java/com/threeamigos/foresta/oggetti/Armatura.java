package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

/**
 * Loot a sé, come spada e scudo.
 * (vedi TipoOggetto.ARMATURA e l'elenco degli oggetti del Bosco).
 */
public class Armatura extends OggettoArtefatto {

	public Armatura() {
		super(TipoArtefatto.ARMATURA);
	}

	public String getAIS() {
		return Articoli.UNA;
	}

	public String getAIP() {
		return Articoli.ALCUNE;
	}

	public String getADS() {
		return Articoli.L_APOSTROFO;
	}

	public String getADP() {
		return Articoli.LE;
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
