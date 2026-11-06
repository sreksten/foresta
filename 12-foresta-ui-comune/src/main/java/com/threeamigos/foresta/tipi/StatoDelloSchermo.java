package com.threeamigos.foresta.tipi;

/**
 * Che cosa mostra lo schermo del gioco (vedi <code>SchermoDelGioco</code>): la schermata di gioco a riquadri o una delle schermate
 * a tutto schermo. È lo stesso per Swing e per libGDX.
 */
public enum StatoDelloSchermo {
	/** I loghi, la storia e la classifica, a giro, prima di una partita */
	INTRO,
	/** La scelta del salvataggio da caricare */
	SELEZIONE_SLOT_DA_CARICARE,
	/** Un messaggio a tutto schermo in caratteri grandi (la scelta della lingua, del nome, del sesso, della classe…) */
	MESSAGGIO,
	/** La schermata di gioco, con i riquadri impilati */
	IN_GIOCO,
	/** La mappa a tutto schermo */
	MAPPA,
	/** La scelta del posto in cui salvare */
	SELEZIONE_SLOT_DA_SALVARE,
	/** La conferma prima di uscire dal gioco */
	CONFERMA_USCITA,
	/** La sconfitta, con il suo racconto */
	PERSO,
	/** La vittoria, con il suo racconto */
	VINTO,
	/** Le statistiche di fine partita */
	STATISTICHE,
	/** La classifica dei punteggi migliori */
	PUNTEGGI,
	/** L'inventario del gruppo */
	INVENTARIO,
	/** La pagina dei trofei, aperta dall'inventario */
	TROFEI,
	/** Il bestiario, aperto dall'inventario */
	BESTIARIO,
	/** Le tattiche di un personaggio, aperte dall'inventario */
	TATTICHE,
	/** L'armaiolo o il venditore di pergamene */
	COMMERCIANTE,
	/** L'alchimista */
	ALCHIMISTA,
	/** L'incantatore */
	INCANTATORE,
	/** Una pagina di un intermezzo */
	INTERMEZZO
}
