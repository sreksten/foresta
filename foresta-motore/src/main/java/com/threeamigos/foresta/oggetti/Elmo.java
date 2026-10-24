package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

/**
 * Loot a sé, come spada e scudo.
 * (vedi TipoOggetto.ELMO e l'elenco degli oggetti del Bosco).
 */
public class Elmo extends OggettoArtefatto {

	public Elmo() {
		super(TipoArtefatto.ELMO);
	}

	public String getAIS() {
		return Articoli.UN;
	}

	public String getAIP() {
		return Articoli.ALCUNI;
	}

	public String getADS() {
		return Articoli.L_APOSTROFO;
	}

	public String getADP() {
		return Articoli.GLI;
	}

	public String getNomeSingolare() {
		return "elmo";
	}

	public String getNomePlurale() {
		return "elmi";
	}

	public TipoOggetto getClasse() {
		return TipoOggetto.ELMO;
	}
}
