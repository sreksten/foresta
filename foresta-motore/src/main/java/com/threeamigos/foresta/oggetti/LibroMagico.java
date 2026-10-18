package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.strumenti.Misc;

/**
 * Un libro magico del bottino: un oggetto che diventa un artefatto di tipo libro magico (vedi {@link OggettoArtefatto}).
 */
public class LibroMagico extends OggettoArtefatto {

	public LibroMagico() {
		super(TipoArtefatto.LIBRO_MAGICO);
	}

	public String getAIS() {
		return Misc.UN;
	}

	public String getAIP() {
		return Misc.ALCUNI;
	}

	public String getADS() {
		return Misc.IL;
	}

	public String getADP() {
		return Misc.I;
	}

	public String getNomeSingolare() {
		return "libro magico";
	}

	public String getNomePlurale() {
		return "libri magici";
	}

	public TipoOggetto getClasse() {
		return TipoOggetto.LIBRO_MAGICO;
	}
}
