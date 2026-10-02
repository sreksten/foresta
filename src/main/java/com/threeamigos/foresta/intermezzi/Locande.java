package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;

/**
 * Quanto serve agli intermezzi delle locande, nel bosco o in città, per sapere se il
 * gruppo sta davvero entrando in una locanda e quale.
 */
final class Locande {

	private Locande() {
	}

	/**
	 * Il checkpoint di una locanda nel bosco (INIZIO_LOCAZIONE) scatta all'ingresso nella
	 * locazione stessa, ma lo stesso checkpoint scatterebbe anche entrando in una città
	 * (la cui locanda viene costruita subito, a prescindere da cosa si scelga in piazza):
	 * serve quindi distinguere i due casi, altrimenti l'intermezzo comparirebbe appena
	 * entrati in città, prima ancora di aver scelto "Locanda".
	 */
	static boolean momentoCoerenteConLocazioneCorrente(MomentoIntermezzo momento) {
		ClassiLocazione classe = GruppoGiocatore.getIstanza().getClasseLocazioneCorrente();
		if (momento == MomentoIntermezzo.INIZIO_LOCAZIONE) {
			return classe == ClassiLocazione.LOCANDA;
		}
		if (momento == MomentoIntermezzo.INGRESSO_LOCANDA_IN_CITTA) {
			return classe.getTipoLocazione() == ClassiLocazione.TipoLocazione.CITTA;
		}
		return false;
	}

	/**
	 * Il modello dati della locanda in cui si trova il gruppo (nel bosco o in città), oppure null.
	 */
	static LocazioneMD getLocazioneMDLocandaCorrente() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		ClassiLocazione classe = gruppo.getClasseLocazioneCorrente();
		if (classe != ClassiLocazione.LOCANDA && classe.getTipoLocazione() != ClassiLocazione.TipoLocazione.CITTA) {
			return null;
		}
		return Foresta.getLocazioneMD(gruppo.getCoordinate());
	}

	static String getIdentificativoLocandaCorrente() {
		LocazioneMD locazioneMD = getLocazioneMDLocandaCorrente();
		return locazioneMD == null ? null : locazioneMD.ottieniProprieta(Locanda.LOCANDA_IDENTIFICATIVO);
	}

	/**
	 * Quante volte il gruppo è già entrato nella locanda in cui si trova (0 se mai, o se non è in una locanda).
	 */
	static int getVisiteLocandaCorrente() {
		LocazioneMD locazioneMD = getLocazioneMDLocandaCorrente();
		String visite = locazioneMD == null ? null : locazioneMD.ottieniProprieta(Locanda.LOCANDA_VISITE);
		return visite == null ? 0 : Integer.parseInt(visite);
	}
}
