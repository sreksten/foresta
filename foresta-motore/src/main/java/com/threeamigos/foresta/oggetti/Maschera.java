package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

/**
 * Loot a sé, come spada e scudo.
 * (vedi TipoOggetto.MASCHERA e l'elenco degli oggetti del Bosco).
 */
public class Maschera extends OggettoArtefatto {

	public Maschera() {
		super(TipoArtefatto.MASCHERA);
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
		return "maschera";
	}

	public String getNomePlurale() {
		return "maschere";
	}

	public TipoOggetto getClasse() {
		return TipoOggetto.MASCHERA;
	}
}
