package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.Testi;

/**
 * Come le missioni nominano i luoghi nei loro testi.
 */
public final class TestiDeiLuoghi {

	private TestiDeiLuoghi() {
	}

	/**
	 * Un luogo di quella classe, con l'articolo indeterminativo: "una grotta", "delle rovine".
	 */
	public static String indefinito(TipoLocazione tipo) {
		switch (tipo) {
			case GROTTA:
				return Testi.formatta("missioni", "TestiDeiLuoghi.t1");
			case ROVINE:
				return Testi.formatta("missioni", "TestiDeiLuoghi.t2");
			case BOSCO:
				return Testi.formatta("missioni", "TestiDeiLuoghi.t3");
			case PALUDE:
				return Testi.formatta("missioni", "TestiDeiLuoghi.t4");
			case RADURA:
				return Testi.formatta("missioni", "TestiDeiLuoghi.t5");
			case TEMPIO:
				return Testi.formatta("missioni", "TestiDeiLuoghi.t6");
			case LOCANDA:
				return Testi.formatta("missioni", "TestiDeiLuoghi.t7");
			default:
				return Testi.formatta("missioni", "TestiDeiLuoghi.t8");
		}
	}

	/**
	 * Dentro un luogo di quella classe: "in una grotta", "fra delle rovine".
	 */
	public static String dentro(TipoLocazione tipo) {
		return (tipo == TipoLocazione.ROVINE ? "fra " : "in ") + indefinito(tipo);
	}

	/**
	 * Il nome del posto in quelle coordinate, con l'articolo: "il Tempio del Sole", "le Rovine di Malgaard", "la
	 * grotta"; "il posto" se non c'è.
	 */
	public static String nome(CoordinateMD posto) {
		if (posto == null) {
			return Testi.formatta("missioni", "TestiDeiLuoghi.t9");
		}
		LocazioneMD md = Foresta.getLocazioneMD(posto);
		switch (md.getTipo()) {
			case TEMPIO:
				return Foresta.nomeDelTempio(md);
			case ROVINE:
				return Foresta.nomeDelleRovine(md);
			case GROTTA:
				return Testi.formatta("missioni", "TestiDeiLuoghi.t10");
			case BOSCO:
				return Testi.formatta("missioni", "TestiDeiLuoghi.t11");
			case PALUDE:
				return Testi.formatta("missioni", "TestiDeiLuoghi.t12");
			case RADURA:
				return Testi.formatta("missioni", "TestiDeiLuoghi.t13");
			default:
				return md.getNome() != null ? md.getNome() : "la locanda";
		}
	}
}
