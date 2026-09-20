package com.threeamigos.foresta.eventi;

/**
 *
 * @author Stefano Reksten
 */
public enum TipoEvento {


    // Azioni che il giocatore vorrebbe intraprendere

    /**
     * Il giocatore invia un testo al motore
     */
    TESTO_DISPONIBILE,
    /**
     * Il giocatore invia un comando di gioco (generico) all'automa
     */
    COMANDO_DI_GIOCO,
    /**
     * Richiesta di interazione con l'inventario di gruppo
     */
    RICHIESTA_APERTURA_INVENTARIO_GRUPPO,
    /**
     * Richiesta di interazione con un commerciante col quale si può fare una compravendita (ad es., l'Armaiolo)
     */
    RICHIESTA_APERTURA_INVENTARIO_COMMERCIANTE,
    /**
     * Richiede di spostare un oggetto dall'inventario ad un personaggio
     */
    RICHIESTA_SPOSTAMENTO_OGGETTO,
    /**
     * Richiesta di interazione con un fornitore col quale si può fare un acquisto (ad es., l'Alchimista)
     */
    RICHIESTA_APERTURA_INVENTARIO_FORNITORE,
    /**
     * Richiesta di acquisto di un Consumabile da un commerciante
     */
    RICHIESTA_ACQUISTO_CONSUMABILE,
    /**
     * Richiede di visualizzare la mappa conosciuta della foresta a schermo intero
     */
    RICHIESTA_VISUALIZZAZIONE_MAPPA,


    // Interazioni dirette che il motore può avere con un giocatore


    /**
     * Il gioco chiede un testo (ad esempio il nome del personaggio)
     */
    RICHIESTA_TESTO,
    /**
     * Richiesta di selezione di uno slot per effettuare un salvataggio
     */
    RICHIESTA_SELEZIONE_SLOT_PER_SALVATAGGIO,
    /**
     * Richiesta di selezione di uno slot per effettuare una rilettura
     */
    RICHIESTA_SELEZIONE_SLOT_PER_RILETTURA,
    /**
     * Il motore approva lo spostamento di un oggetto da un inventario ad un altro
     * (Personaggio <-> inventario di gruppo oppure inventario di gruppo <-> inventario di un commerciante)
     */
    APPROVAZIONE_SPOSTAMENTO_OGGETTO,
    /**
     * Il motore non approva lo spostamento di un oggetto da un inventario ad un altro
     * (Personaggio <-> inventario di gruppo oppure inventario di gruppo <-> inventario di un commerciante)
     */
    RIFIUTO_SPOSTAMENTO_OGGETTO,
    /**
     * Il motore approva l'acquisto di un Consumabile da un Fornitore
     */
    APPROVAZIONE_ACQUISTO_CONSUMABILE,
    /**
     * Il motore non approva l'acquisto di un Consumabile da un Fornitore
     */
    RIFIUTO_ACQUISTO_CONSUMABILE,
    /**
     * Il motore chiede conferma per l'uscita dal gioco
     */
    RICHIESTA_CONFERMA_USCITA,


    // Notifiche dal motore al giocatore riguardanti l'avanzamento del gioco

    /**
     * Raccoglie gli oggetti vinti agli avversari
     */
    RACCOLTA_OGGETTI,
    /**
     * Variazione delle gemme disponibili al gruppo
     */
    VARIAZIONE_GEMME,
    /**
     * Variazione delle monete disponibili al gruppo
     */
    VARIAZIONE_MONETE,
    /**
     * Variazione del punteggio globale
     */
    VARIAZIONE_PUNTI,
    /**
     * Variazione dei punti esperienza di un singolo Personaggio
     */
    VARIAZIONE_PUNTI_ESPERIENZA,
    /**
     * Variazione degli incantesimi disponibili al gruppo
     */
    VARIAZIONE_INCANTESIMI,
    /**
     * Variazione della mappa conosciuta della foresta
     */
    VARIAZIONE_MAPPA,
    /**
     * Variazione della quantità di pozioni salute disponibili
     */
    VARIAZIONE_POZIONI_SALUTE,
    /**
     * Variazione della quantità di pozioni salute grandi disponibili
     */
    VARIAZIONE_POZIONI_SALUTE_GRANDI,
    /**
     * Variazione della quantità di pozioni magia disponibili
     */
    VARIAZIONE_POZIONI_MAGIA,
    /**
     * Variazione della quantità di pozioni magia grandi disponibili
     */
    VARIAZIONE_POZIONI_MAGIA_GRANDI,
    /**
     * Un Personaggio aumenta di livello
     */
    PERSONAGGIO_AUMENTO_LIVELLO,
    /**
     * Un Personaggio muore o resuscita
     */
    PERSONAGGIO_VARIAZIONE_STATO_VITALE,
    /**
     * Un Personaggio acquisisce un Modificatore che ne cambia le statistiche
     */
    PERSONAGGIO_AGGIUNTA_MODIFICATORE,
    /**
     * Un Personaggio consuma un punto di abilità per aumentare uno dei suoi attributi primari
     */
    PERSONAGGIO_CONSUMO_PUNTO_ABILITA,
    /**
     * Un Personaggio subisce una variazione delle sue statistiche
     */
    PERSONAGGIO_VARIAZIONE_STATISTICHE,
    /**
     * Un Personaggio combatta con un altro Personaggio
     */
    PERSONAGGIO_COMBATTIMENTO,
    /**
     * Un Personaggio subisce una variazione di un effetto di stato come effetto collaterale di un combattimento
     */
    PERSONAGGIO_VARIAZIONE_EFFETTO_DI_STATO,
    /**
     * Un Personaggio subisce una interazione elementale come effetto collaterale di un combattimento
     */
    PERSONAGGIO_INTERAZIONE_ELEMENTALE,
    /**
     * Il gioco aumenta di difficoltà
     */
    MONDO_AUMENTO_LIVELLO,
    /**
     * Notifica sull'aggiornamento di uno stato missione
     */
    AGGIORNAMENTO_STATO_MISSIONE,
    /**
     * Mostra un annuncio in evidenza (ad esempio l'inizio di una missione)
     */
    NOTIFICA_GLOBALE,
    /**
     * Nuovo paragrafo con spaziatura antecedente
     */
    PARAGRAFO,
    /**
     * Continuazione del paragrafo precedente
     */
    MESSAGGIO,
    /**
     * Mostra un fumetto a video
     */
    FUMETTO,
    /**
     * Mostra al giocatore o chiude la finestra del combattimento
     */
    VISUALIZZA_FINESTRA_COMBATTIMENTO,
    /**
     * Fine della partita
     */
    FINE_GIOCO,
    /**
     * Mostra le statistiche sulla partita appena conclusa
     */
    MOSTRA_STATISTICHE,
    /**
     * Mostra i migliori 10 giocatori di tutti i tempi
     */
    MOSTRA_PUNTEGGI,


    // Eventi interni per il funzionamento del gioco

    /**
     * Richiede una reinizializzazione dell'interfaccia grafica
     */
    REINIZIALIZZAZIONE,
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
     * Un nuovo Personaggio viene creato dal motore
     */
    PERSONAGGIO_CREAZIONE,
    /**
     * PRepara la locazione corrente per il turno di gioco
     */
    PREPARAZIONE_LOCAZIONE,
    /**
     * Un PNG prende una decisione riguardante un combattimento
     */
    PERSONAGGIO_VALUTAZIONE,


    // Eventi interni del motore


    /**
     * Interfaccia utente inizializzata - segnala che il sistema è pronto per il gioco
     */
    INTERFACCIA_UTENTE_PRONTA,
    /**
     * Il motore chiede alla UI di mostrare la finestra principale di gioco
     */
    MOSTRA_SCHERMATA_GIOCO,
    /**
     * Il motore chiede alla UI di portare in primo piano una certa Finestra
     */
    MOSTRA_FINESTRA,
    /**
     * Chiede alla UI un refresh
     */
    REFRESH,
    /**
     * Un componente interno crea uno sprite di "annuncio globale" e lo notifica al gestore grafico
     */
    CREAZIONE_SPRITE_ANNUNCIO_GLOBALE,
    /**
     * Un componente interno crea uno SpriteATempo e lo notifica al gestore grafico
     */
    CREAZIONE_SPRITE_A_TEMPO,
    /**
     * Un componente interno crea uno Sprite effetto e lo notifica al gestore grafico
     */
    CREAZIONE_SPRITE_EFFETTO,
    /**
     * Un componente interno crea uno Sprite fumetto e lo notifica al gestore grafico
     */
    CREAZIONE_SPRITE_FUMETTO,
    /**
     * Un componente interno crea uno Sprite in dissolvenza e lo notifica al gestore grafico
     */
    CREAZIONE_SPRITE_IN_DISSOLVENZA,
    /**
     * Attività interna di pulizia cache dinamica immagini
     */
    PULIZIA_CACHE_IMMAGINI,
    /**
     * Messaggi di notifica interni al motore non destinati al giocatore
     */
    MESSAGGIO_INTERNO,
    /**
     * Errore interno del motore
     */
    ERRORE_INTERNO,

}
