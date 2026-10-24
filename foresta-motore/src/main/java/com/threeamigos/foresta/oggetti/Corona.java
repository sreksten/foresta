package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoOggetto;

/**
 * Alcune corone: per il gruppo valgono come le pietre preziose e si sommano ai preziosi.
 */
public class Corona extends OggettoBase implements Oggetto {

	public Corona() {
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
		return "corona";
	}

	public String getNomePlurale() {
		return "corone";
	}

	public TipoOggetto getClasse() {
		return TipoOggetto.CORONA;
	}

}
