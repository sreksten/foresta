package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoBottino;

/**
 * Alcune monete d'oro: prese, si aggiungono alle monete del gruppo.
 */
public class Moneta extends OggettoBase implements Bottino {

	public Moneta() {
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
		return "moneta";
	}

	public String getNomePlurale() {
		return "monete";
	}

	public TipoBottino getClasse() {
		return TipoBottino.MONETA;
	}

}

