package com.threeamigos.foresta.tools;

/**
 * La modalità di prova, per chi sviluppa: si attiva con la system property testMode a true, yes o 1 (es.
 * -DtestMode=true). Accende le parti che truccano la partita per provarla in fretta: monete, pergamene e pozioni
 * a volontà, la mappa già svelata, artefatti potenti e punti abilità al primo personaggio, le missioni e
 * l'intermezzo di prova. Senza, la partita parte come per un giocatore vero.
 */
public final class ModalitaDiProva {

	private static boolean attiva;

	private ModalitaDiProva() {
	}

	public static void setAttiva(boolean attiva) {
		ModalitaDiProva.attiva = attiva;
	}

	public static boolean isAttiva() {
		return attiva;
	}
}
