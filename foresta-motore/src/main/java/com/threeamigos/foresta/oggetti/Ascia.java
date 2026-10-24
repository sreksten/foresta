package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

/**
 * Un'ascia del bottino: un oggetto che diventa un artefatto di tipo ascia (vedi {@link OggettoArtefatto}).
 */
public class Ascia extends OggettoArtefatto {

	public Ascia() {
		super(TipoArtefatto.ASCIA);
	}

	public String getAIS() {
		return Articoli.UN_APOSTROFO;
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
		return "ascia";
	}

	public String getNomePlurale() {
		return "asce";
	}

	public TipoOggetto getClasse() {
		return TipoOggetto.ASCIA;
	}
}
