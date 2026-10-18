package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.strumenti.Misc;

/**
 * Un'ascia del bottino: un oggetto che diventa un artefatto di tipo ascia (vedi {@link OggettoArtefatto}).
 */
public class Ascia extends OggettoArtefatto {

	public Ascia() {
		super(TipoArtefatto.ASCIA);
	}

	public String getAIS() {
		return Misc.UN_APOSTROFO;
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
		return "ascia";
	}

	public String getNomePlurale() {
		return "asce";
	}

	public TipoOggetto getClasse() {
		return TipoOggetto.ASCIA;
	}
}
