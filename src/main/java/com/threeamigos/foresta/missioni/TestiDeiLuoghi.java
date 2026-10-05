package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.locazioni.Rovine;
import com.threeamigos.foresta.locazioni.Tempio;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * Come le missioni nominano i luoghi nei loro testi.
 */
final class TestiDeiLuoghi {

	private TestiDeiLuoghi() {
	}

	/**
	 * Un luogo di quella classe, con l'articolo indeterminativo: "una grotta", "delle rovine".
	 */
	static String indefinito(TipoLocazione classe) {
		switch (classe) {
			case GROTTA:
				return "una grotta";
			case ROVINE:
				return "delle rovine";
			case BOSCO:
				return "un bosco";
			case PALUDE:
				return "una palude";
			case RADURA:
				return "una radura";
			case TEMPIO:
				return "un tempio";
			case LOCANDA:
				return "una locanda";
			default:
				return "un posto";
		}
	}

	/**
	 * Dentro un luogo di quella classe: "in una grotta", "fra delle rovine".
	 */
	static String dentro(TipoLocazione classe) {
		return (classe == TipoLocazione.ROVINE ? "fra " : "in ") + indefinito(classe);
	}

	/**
	 * Il nome del posto in quelle coordinate, con l'articolo: "il Tempio del Sole", "le Rovine di Malgaard", "la
	 * grotta"; "il posto" se non c'è.
	 */
	static String nome(CoordinateMD posto) {
		if (posto == null) {
			return "il posto";
		}
		LocazioneMD md = Foresta.getLocazioneMD(posto);
		switch (md.getClasse()) {
			case TEMPIO:
				return Tempio.getNome(md);
			case ROVINE:
				return Rovine.getNome(md);
			case GROTTA:
				return "la grotta";
			case BOSCO:
				return "il bosco";
			case PALUDE:
				return "la palude";
			case RADURA:
				return "la radura";
			default:
				return md.getNome() != null ? md.getNome() : "la locanda";
		}
	}
}
