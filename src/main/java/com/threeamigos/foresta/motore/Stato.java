package com.threeamigos.foresta.motore;

/**
 * Gli stati in cui l'automa che controlla lo stato del gioco si pu� trovare
 */
public enum Stato {

	INTRO, // Prima dell'inizio del gioco vero e proprio,
	
	CONTROLLO_SALVATAGGI,
	SELEZIONE_NUOVO_GIOCO_O_CARICA,
	SELEZIONE_SALVATAGGIO_DA_LEGGERE,
	LETTURA_SALVATAGGIO,
	
	PRE_GAME_SELEZIONE_PERSONAGGIO,
	PRE_GAME_ATTESA_NOME_PERSONAGGIO,
	PRE_GAME_ATTESA_SESSO_PERSONAGGIO,
	PRE_GAME_ATTESA_CLASSE_PERSONAGGIO,
	
	INIZIALIZZAZIONE_GIOCO,

	INZIO_LOCAZIONE, // Crea una nuova locazione, crea i mostri, descrive
	// e passa al successivo
	IN_LOCAZIONE, // Stabilisce quali azioni possono essere intraprese

	SCELTA_AUTOMATICA_PERSONAGGIO,
	SCELTA_PERSONAGGIO_QUALSIASI,
	SCELTA_MANUALE_PERSONAGGIO,

	IN_COMBATTIMENTO,

	SCELTA_INCANTESIMO_DA_LANCIARE,
	ATTESA_INCANTESIMO_QUALSIASI,
	INCANTESIMO_SCELTO,

	ATTESA_SI_NO,

	FINE_LOCAZIONE,
	ATTESA_DIREZIONE,
	ATTESA_PASSI,
	
	IN_CAMMINO,

	ATTESA_FORZA,
	ATTESA_GRANDE_FORZA,
	ATTESA_MAGIA,
	MAPPA,
	
	SELEZIONE_SALVATAGGIO_DA_SCRIVERE,
	CONFERMA_USCITA,

	GIOCO_PERSO,
	GIOCO_PERSO_2,
	GIOCO_VINTO,
	GIOCO_VINTO_2,
	STATISTICHE,
	ATTESA_NOME_HI_SCORE,
	HI_SCORE
	
}