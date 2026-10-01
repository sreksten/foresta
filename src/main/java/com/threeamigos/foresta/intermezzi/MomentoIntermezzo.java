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
	INIZIO_LOCAZIONE,

	/**
	 * Alla fine di una locazione, dopo i controlli post-locazione delle missioni,
	 * prima che il gruppo riparta verso una nuova direzione.
	 */
	LOCAZIONE_COMPLETATA,

	/**
	 * Quando dalla piazza di una città si sceglie di entrare nella locanda, prima che
	 * {@link com.threeamigos.foresta.locazioni.Citta} esegua il comando.
	 */
	INGRESSO_LOCANDA_IN_CITTA,

	/**
	 * Quando dalla piazza di una città si sceglie di entrare dall'alchimista, prima che
	 * {@link com.threeamigos.foresta.locazioni.Citta} esegua il comando.
	 */
	INGRESSO_ALCHIMISTA,

	/**
	 * Quando dalla piazza di una città si sceglie di entrare dall'armaiolo, prima che
	 * {@link com.threeamigos.foresta.locazioni.Citta} esegua il comando.
	 */
	INGRESSO_ARMAIOLO,

	/**
	 * Quando dalla piazza di una città si sceglie di entrare dal venditore di pergamene,
	 * prima che {@link com.threeamigos.foresta.locazioni.Citta} esegua il comando.
	 */
	INGRESSO_VENDITORE_DI_PERGAMENE,

	/**
	 * Quando dalla piazza di una città si sceglie di entrare dall'incantatore, prima che
	 * {@link com.threeamigos.foresta.locazioni.Citta} esegua il comando.
	 */
	INGRESSO_INCANTATORE
}
