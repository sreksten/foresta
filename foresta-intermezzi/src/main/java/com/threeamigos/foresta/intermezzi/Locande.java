package com.threeamigos.foresta.intermezzi;

import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.tipi.CategoriaLocazione;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * Quanto serve agli intermezzi delle locande, nel bosco o in città, per sapere se il
 * gruppo sta davvero entrando in una locanda e quale.
 */
public final class Locande {

	/**
	 * Segnata sulla casella quando il gruppo supera la porta: chi viene respinto
	 * dall'oste per mancanza di monete non è entrato, e non ha visitato la locanda.
	 */
	public static final String LOCANDA_VISITATA = "LOCANDA_VISITATA";
	/**
	 * Quante volte il gruppo ha superato la porta della locanda.
	 */
	public static final String LOCANDA_VISITE = "LOCANDA_VISITE";
	/**
	 * Identificativo, recensione e dialogo della locanda, pescati quando la locanda viene costruita (vedi Locanda).
	 */
	public static final String LOCANDA_IDENTIFICATIVO = "LOCANDA_IDENTIFICATIVO";
	public static final String LOCANDA_RECENSIONE = "LOCANDA_RECENSIONE";
	public static final String LOCANDA_DIALOGO = "LOCANDA_DIALOGO";
	/**
	 * Segnate quando il dialogo/la recensione sono già stati mostrati, per non ripeterli
	 * alle visite successive.
	 */
	public static final String LOCANDA_DIALOGO_LETTO = "LOCANDA_DIALOGO_LETTO";
	public static final String LOCANDA_RECENSIONE_LETTA = "LOCANDA_RECENSIONE_LETTA";

	/**
	 * Il nome di una locanda in città: quello della casella è il nome della città (vedi getNome).
	 */
	public static final String LOCANDA_NOME_IN_CITTA = "LOCANDA_NOME_IN_CITTA";

	private Locande() {
	}

	/**
	 * Il nome della locanda della casella, nel bosco o in una città.
	 */
	public static String getNome(LocazioneMD modelloDati) {
		return modelloDati.getTipo() == TipoLocazione.LOCANDA ? modelloDati.getNome() : modelloDati.ottieniProprieta(LOCANDA_NOME_IN_CITTA);
	}

	/**
	 * Il checkpoint di una locanda nel bosco (INIZIO_LOCAZIONE) scatta all'ingresso nella
	 * locazione stessa, ma lo stesso checkpoint scatterebbe anche entrando in una città
	 * (la cui locanda viene costruita subito, a prescindere da cosa si scelga in piazza):
	 * serve quindi distinguere i due casi, altrimenti l'intermezzo comparirebbe appena
	 * entrati in città, prima ancora di aver scelto "Locanda".
	 */
	public static boolean momentoCoerenteConLocazioneCorrente(MomentoIntermezzo momento) {
		TipoLocazione tipo = Foresta.getLocazioneDelGruppo();
		if (momento == MomentoIntermezzo.INIZIO_LOCAZIONE) {
			return tipo == TipoLocazione.LOCANDA;
		}
		if (momento == MomentoIntermezzo.INGRESSO_LOCANDA_IN_CITTA) {
			return tipo.getCategoriaLocazione() == CategoriaLocazione.CITTA;
		}
		return false;
	}

	/**
	 * Il modello dati della locanda in cui si trova il gruppo (nel bosco o in città), oppure null.
	 */
	static LocazioneMD getLocazioneMDLocandaCorrente() {
		GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
		TipoLocazione tipo = Foresta.getLocazioneDelGruppo();
		if (tipo != TipoLocazione.LOCANDA && tipo.getCategoriaLocazione() != CategoriaLocazione.CITTA) {
			return null;
		}
		return Foresta.getLocazioneMD(Foresta.getPosizioneDelGruppo());
	}

	static String getIdentificativoLocandaCorrente() {
		LocazioneMD locazioneMD = getLocazioneMDLocandaCorrente();
		return locazioneMD == null ? null : locazioneMD.ottieniProprieta(LOCANDA_IDENTIFICATIVO);
	}

	/**
	 * Quante volte il gruppo è già entrato nella locanda in cui si trova (0 se mai, o se non è in una locanda).
	 */
	static int getVisiteLocandaCorrente() {
		LocazioneMD locazioneMD = getLocazioneMDLocandaCorrente();
		String visite = locazioneMD == null ? null : locazioneMD.ottieniProprieta(LOCANDA_VISITE);
		return visite == null ? 0 : Integer.parseInt(visite);
	}
}
