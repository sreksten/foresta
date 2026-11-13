package com.threeamigos.foresta.strumenti;

/**
 * La modalità di prova, per chi sviluppa: si attiva con l'argomento di avvio MODALITA_DI_PROVA
 * (vedi Main). Accende le parti che truccano la partita per provarla in fretta: monete, pergamene e pozioni
 * a volontà, la mappa già svelata, artefatti potenti e punti abilità al primo personaggio, le missioni e
 * l'intermezzo di prova. Senza, la partita parte come per un giocatore vero.
 */
public final class ModalitaDiProva {

	// Statico: vale per tutto il programma, lo si decide una volta all'avvio
	private static boolean attiva;

	// Una classe di soli metodi statici: non si istanzia
	private ModalitaDiProva() {
	}

	/**
	 * Accende o spegne la modalità di prova; si chiama all'avvio, prima di creare la partita.
	 */
	public static void setAttiva(boolean attiva) {
		ModalitaDiProva.attiva = attiva;
	}

	/**
	 * Se la modalità di prova è accesa.
	 */
	public static boolean isAttiva() {
		return attiva;
	}
}
