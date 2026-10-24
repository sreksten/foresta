package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoOggetto;

/**
 * Un libro magico del bottino: un oggetto che diventa un artefatto di tipo libro magico (vedi {@link OggettoArtefatto}).
 */
public class LibroMagico extends OggettoArtefatto {

	public LibroMagico() {
		super(TipoArtefatto.LIBRO_MAGICO);
	}

	public String getAIS() {
		return Articoli.UN;
	}

	public String getAIP() {
		return Articoli.ALCUNI;
	}

	public String getADS() {
		return Articoli.IL;
	}

	public String getADP() {
		return Articoli.I;
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
