package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
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
				return ProduttoreDiMissioniCasuale.testoDellaMissione("TESTI_DEI_LUOGHI_UNA_GROTTA");
			case ROVINE:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("TESTI_DEI_LUOGHI_DELLE_ROVINE");
			case BOSCO:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("TESTI_DEI_LUOGHI_UN_BOSCO");
			case PALUDE:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("TESTI_DEI_LUOGHI_UNA_PALUDE");
			case RADURA:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("TESTI_DEI_LUOGHI_UNA_RADURA");
			case TEMPIO:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("TESTI_DEI_LUOGHI_UN_TEMPIO");
			case LOCANDA:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("TESTI_DEI_LUOGHI_UNA_LOCANDA");
			default:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("TESTI_DEI_LUOGHI_UN_POSTO");
		}
	}

	/**
	 * Dentro un luogo di quella classe: "in una grotta", "fra delle rovine".
	 */
	public static String dentro(TipoLocazione tipo) {
		return ProduttoreDiMissioniCasuale.testoDellaMissione(tipo == TipoLocazione.ROVINE ? "TESTI_DEI_LUOGHI_DENTRO_ROVINE" : "TESTI_DEI_LUOGHI_DENTRO") + " " + indefinito(tipo);
	}

	/**
	 * Il nome del posto in quelle coordinate, con l'articolo: "il Tempio del Sole", "le Rovine di Malgaard", "la
	 * grotta"; "il posto" se non c'è.
	 */
	public static String nome(CoordinateMD posto) {
		if (posto == null) {
			return ProduttoreDiMissioniCasuale.testoDellaMissione("TESTI_DEI_LUOGHI_IL_POSTO");
		}
		LocazioneMD md = Foresta.getLocazioneMD(posto);
		switch (md.getTipo()) {
			case TEMPIO:
				return Foresta.nomeDelTempio(md);
			case ROVINE:
				return Foresta.nomeDelleRovine(md);
			case GROTTA:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("TESTI_DEI_LUOGHI_LA_GROTTA");
			case BOSCO:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("TESTI_DEI_LUOGHI_IL_BOSCO");
			case PALUDE:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("TESTI_DEI_LUOGHI_LA_PALUDE");
			case RADURA:
				return ProduttoreDiMissioniCasuale.testoDellaMissione("TESTI_DEI_LUOGHI_LA_RADURA");
			default:
				return md.getNome() != null ? md.getNome() : ProduttoreDiMissioniCasuale.testoDellaMissione("TESTI_DEI_LUOGHI_LA_LOCANDA");
		}
	}
}
