package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoBottino;

/**
 * Alcune pietre preziose: prese, si aggiungono ai preziosi del gruppo.
 */
public class PietraPreziosa extends OggettoBase implements Bottino {

	public PietraPreziosa() {
		super();
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
		return "pietra preziosa";
	}

	public String getNomePlurale() {
		return "pietre preziose";
	}

	public TipoBottino getClasse() {
		return TipoBottino.PIETRA_PREZIOSA;
	}

}
