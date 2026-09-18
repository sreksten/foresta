package com.threeamigos.foresta.eventi;

/**
 *
 * @author Stefano Reksten
 */
public enum TipoEvento {

    // Notifiche dal motore al giocatore

    // Nuovo paragrafo con spaziatura antecedente
    PARAGRAFO,
    // Continuazione del paragrafo precedente
    MESSAGGIO,
    // Mostra un fumetto a video
    FUMETTO,
    // Mostra un annuncio in evidenza
    NOTIFICA_GLOBALE,

    // Azioni del giocatore

    // Richiede di interagire con l'inventario
    RICHIESTA_INVENTARIO,
    // Richiede di spostare un oggetto dall'inventario ad un personaggio
    RICHIESTA_SPOSTAMENTO_OGGETTO,
    // L'oggetto può essere spostato dall'inventario ad un personaggio
    APPROVAZIONE_SPOSTAMENTO_OGGETTO,
    // L'oggetto non può essere spostato dall'inventario ad un personaggio
    RIFIUTO_SPOSTAMENTO_OGGETTO,

    // Richiesta di acquisto di un Consumabile da un commerciante
    RICHIESTA_ACQUISTO_CONSUMABILE,
    // L'oggetto può essere acquistato da un commerciante
    APPROVAZIONE_ACQUISTO_CONSUMABILE,
    // L'oggetto non può essere acquistato da un commerciante
    RIFIUTO_ACQUISTO_CONSUMABILE,

    // Richiede di visualizzare la mappa conosciuta della foresta a schermo intero
    RICHIESTA_VISUALIZZAZIONE_MAPPA,

    // Notifiche dal motore all'interfaccia utente

    // Variazione delle gemme disponibili al gruppo
    VARIAZIONE_GEMME,
    // Variazione delle monete disponibili al gruppo
    VARIAZIONE_MONETE,
    // Variazione del punteggio globale
    VARIAZIONE_PUNTI,
    // Vairazione dei punti esperienza di un singolo Personaggio
    VARIAZIONE_PUNTI_ESPERIENZA,
    // Variazione degli incantesimi disponibili al gruppo
    VARIAZIONE_INCANTESIMI,
    // Variazione della mappa conosciuta della foresta
    VARIAZIONE_MAPPA,
    // Variazione della quantità di pozioni salute disponibili
    VARIAZIONE_POZIONI_SALUTE,
    // Variazione della quantità di pozioni salute grandi disponibili
    VARIAZIONE_POZIONI_SALUTE_GRANDI,
    // Variazione della quantità di pozioni magia disponibili
    VARIAZIONE_POZIONI_MAGIA,
    // Variazione della quantità di pozioni magia grandi disponibili
    VARIAZIONE_POZIONI_MAGIA_GRANDI,

    PERSONAGGIO_CREAZIONE,
    PERSONAGGIO_VARIAZIONE_STATO_VITALE,
    PERSONAGGIO_VARIAZIONE_STATISTICHE,
    PERSONAGGIO_VARIAZIONE_EFFETTO_DI_STATO,
    PERSONAGGIO_AGGIUNTA_MODIFICATORE,
    PERSONAGGIO_CONSUMO_PUNTO_ABILITA,
    PERSONAGGIO_COMBATTIMENTO,
    PERSONAGGIO_INTERAZIONE_ELEMENTALE,
    PERSONAGGIO_AUMENTO_LIVELLO,

    MONDO_AUMENTO_LIVELLO,

    /**
     * Un PNG prende una decisione
     */
    PERSONAGGIO_VALUTAZIONE,

    /**
     * Il gioco chiede un testo (ad esempio il nome del personaggio)
     */
    RICHIESTA_TESTO,
    /**
     * Il gioco riceve il testo
     */
    TESTO_DISPONIBILE,
    /**
     * Il giocatore invia un comando all'automa
     */
    COMANDO_DI_GIOCO,

    // Eventi interni per il funzionamento del gioco

    /**
     * Interfaccia utente inizializzata - segnala che il sistema è pronto per il gioco
     */
    INTERFACCIA_UTENTE_PRONTA,
    /**
     * Richiede una reinizializzazione dell'interfaccia grafica
     */
    REINIZIALIZZAZIONE,
    /**
     * Richiesta di selezione di uno slot per effettuare un salvataggio
     */
    RICHIESTA_SELEZIONE_SLOT_PER_SALVATAGGIO,
    /**
     * Richiesta di selezione di uno slot per effettuare una rilettura
     */
    RICHIESTA_SELEZIONE_SLOT_PER_RILETTURA,
    /**
     * Errore di caricamento del gioco
     */
    ERRORE_CARICAMENTO,
    /**
     * Elenco dei possibili comandi che l'interfaccia deve mostrare
     */
    COMANDI_DISPONIBILI,
    /**
     * Cambio di stato dell'automa principale che informa la UI
     */
    STATO_DI_GIOCO,
    /**
     * Attività interna di pulizia cache dinamica immagini
     */
    PULIZIA_CACHE_IMMAGINI,
    /**
     * Messaggi di notifica interni al motore non destinati al giocatore
     */
    MESSAGGIO_INTERNO,
    ERRORE_INTERNO,
    /**
     * Creazione di uno sprite "annuncio globale"
     */
    CREAZIONE_SPRITE_ANNUNCIO_GLOBALE,
    /**
     * Creazione di uno SpriteATempo
     */
    CREAZIONE_SPRITE_A_TEMPO,
    /**
     * Creazione di uno Sprite effetto
     */
    CREAZIONE_SPRITE_EFFETTO,
    /**
     * Creazione di uno Sprite fumetto
     */
    CREAZIONE_SPRITE_FUMETTO,
    /**
     * Creazione di uno Sprite in dissolvenza
     */
    CREAZIONE_SPRITE_IN_DISSOLVENZA
}
