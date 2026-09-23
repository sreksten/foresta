package com.threeamigos.foresta.motore;

/**
 * Gli stati in cui l'automa che controlla lo stato del gioco si può trovare
 */
public enum Stato {

	// Prima dell'inizio del gioco vero e proprio
	INTRO,

	// Inizio gioco - selezione partita precedente o creazione nuovo personaggio
	PRE_GAME_SELEZIONE_SALVATAGGIO_DA_LEGGERE,
	FILE_DI_SALVATAGGIO_NON_VALIDO,
	PRE_GAME_ATTESA_NOME_PERSONAGGIO,
	PRE_GAME_ATTESA_SESSO_PERSONAGGIO,
	PRE_GAME_ATTESA_CLASSE_PERSONAGGIO,

	// Eventi in gioco

	// Crea una nuova locazione, crea i mostri, descrive, controlla trigger pre-locazione e passa al successivo
	INZIO_LOCAZIONE,
	// Controlla trigger in-locazione, stabilisce quali azioni possono essere intraprese
	IN_LOCAZIONE,

	SCELTA_AUTOMATICA_PERSONAGGIO,
	SCELTA_PERSONAGGIO_QUALSIASI,
	SCELTA_MANUALE_PERSONAGGIO,

	IN_COMBATTIMENTO,

	INVENTARIO,

	SCELTA_INCANTESIMO_DA_LANCIARE,
	ATTESA_INCANTESIMO_QUALSIASI,
	INCANTESIMO_SCELTO,

	ATTESA_SI_NO,

	// Controlla trigger post-locazione
	FINE_LOCAZIONE,
	// Pubblica la richiesta della direzione e passa subito a SCELTA_DIREZIONE
	ATTESA_DIREZIONE,
	// Attende la direzione (o pozioni, mappa, inventario, accampamento...)
	SCELTA_DIREZIONE,
	// Attende il numero di passi; ANNULLA riporta ad ATTESA_DIREZIONE
	SCELTA_PASSI,

	ATTESA_POZIONE_SALUTE,
	ATTESA_POZIONE_SALUTE_GRANDE,
	ATTESA_POZIONE_MAGIA,
	ATTESA_POZIONE_MAGIA_GRANDE,
	SCELTA_BERSAGLIO_RESURREZIONE,
	ESEECUZIONE_RESURREZIONE,
	MAPPA,

	// Gestione salvataggi
	SELEZIONE_SALVATAGGIO_DA_SCRIVERE,
	CONFERMA_USCITA,

	// Fine del gioco
	GIOCO_PERSO,
	GIOCO_PERSO_2,
	GIOCO_VINTO,
	GIOCO_VINTO_2,
	STATISTICHE,
	ATTESA_NOME_PUNTEGGI,
	PUNTEGGI

}