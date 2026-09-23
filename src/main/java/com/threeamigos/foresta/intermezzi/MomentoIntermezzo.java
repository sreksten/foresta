package com.threeamigos.foresta.intermezzi;

/**
 * I punti del gioco in cui l'automa controlla se qualche intermezzo deve scattare.
 */
public enum MomentoIntermezzo {

	/**
	 * Una volta sola, all'inizio di una nuova partita: dopo la creazione del personaggio,
	 * prima dei controlli delle missioni e della prima locazione.
	 */
	INIZIO_GIOCO,

	/**
	 * All'arrivo in una nuova locazione, dopo i controlli pre-locazione delle missioni
	 * e gli eventi della linea temporale, prima che la locazione venga costruita.
	 */
	INIZIO_LOCAZIONE
}
