package com.threeamigos.foresta.motore;

/**
 * Gli stati in cui l'automa che controlla lo stato del gioco si può trovare
 */
public enum Stato {

	// Il primissimo stato, solo all'avvio: il logo 3AM tracciato una volta mentre si caricano le risorse.
	// Non ci si torna piu': dopo la partita si riparte da INTRO.
	LOGO_INIZIALE,

	// Prima dell'inizio del gioco vero e proprio
	INTRO,

	// Inizio gioco - selezione partita precedente o creazione nuovo personaggio
	PRE_GAME_SELEZIONE_SALVATAGGIO_DA_LEGGERE,
	FILE_DI_SALVATAGGIO_NON_VALIDO,
	PRE_GAME_ATTESA_NOME_PERSONAGGIO,
	PRE_GAME_ATTESA_SESSO_PERSONAGGIO,
	PRE_GAME_ATTESA_CLASSE_PERSONAGGIO,
	// Personaggio creato: mostra gli intermezzi di inizio partita e passa a INZIO_LOCAZIONE
	INIZIO_GIOCO,

	// Eventi in gioco

	// Crea una nuova locazione, crea i mostri, descrive, controlla trigger pre-locazione e passa al successivo
	INZIO_LOCAZIONE,
	// Mostra le pagine di un intermezzo e attende il click o il timer per avanzare
	INTERMEZZO,
	// Un intermezzo è pronto a scattare ma si attende che la UI finisca le animazioni in corso
	ATTESA_UI_PER_INTERMEZZO,
	// Costruisce la locazione, la descrive e ne imposta le azioni
	PREPARAZIONE_LOCAZIONE,
	// Controlla trigger in-locazione, stabilisce quali azioni possono essere intraprese
	IN_LOCAZIONE,
	// Si entra in un negozio di città (locanda compresa): dà modo al suo intermezzo di
	// scattare, poi esegue il comando con cui ci si era entrati
	INGRESSO_NEGOZIO,
	// Il gruppo si accampa: dà modo al suo intermezzo di scattare, poi fa passare la notte
	ACCAMPAMENTO,

	SCELTA_AUTOMATICA_PERSONAGGIO,
	SCELTA_PERSONAGGIO_QUALSIASI,
	SCELTA_MANUALE_PERSONAGGIO,
	// Chi raccoglie l'oggetto di fine locazione: uno dei personaggi vivi o l'inventario del gruppo
	SCELTA_DESTINATARIO_OGGETTO,

	IN_COMBATTIMENTO,

	INVENTARIO,
	// La pagina dei trofei, aperta dall'inventario: con ANNULLA si torna all'inventario
	TROFEI,

	SCELTA_INCANTESIMO_DA_LANCIARE,
	ATTESA_INCANTESIMO_QUALSIASI,
	INCANTESIMO_SCELTO,

	ATTESA_SI_NO,
	// Una missione a passi ha posto una domanda al giocatore: si aspetta la risposta, poi si rifà il controllo
	// delle missioni in cui è nata e si prosegue da lì (vedi Automa.controllaMissioniEDomande)
	ATTESA_RISPOSTA_MISSIONE,

	// Controlla trigger post-locazione, poi mostra gli intermezzi di fine locazione
	FINE_LOCAZIONE,
	// Coda di FINE_LOCAZIONE dopo gli intermezzi: game over, tempo, stanchezza, ATTESA_DIREZIONE
	FINE_LOCAZIONE_2,
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
	// Resurrezione fuori dalle locazioni: chi la lancia, su chi, esecuzione
	SCELTA_FORMULANTE_RESURREZIONE,
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