package com.threeamigos.foresta.eventi;

/**
 * Gli eventi che accadono nel gioco si distinguono in:
 * <ul>
 *     <li>COMANDI che il giocatore invia al motore</li>
 *     <li>RICHIESTE che il motore fa al giocatore</li>
 *     <li>NOTIFICHE che il motore invia al giocatore</li>
 *     <li>INTERNI</li>
 * </ul>
 *
 * @author Stefano Reksten
 */
public enum TipoEvento {

    // Azioni che il giocatore vorrebbe intraprendere
    /**
     * Richiesta di acquisto di un Consumabile da un commerciante
     */
    COMANDO_ACQUISTO_ARTEFATTO,
    /**
     * Richiesta di acquisto di un Consumabile da un fornitore
     */
    COMANDO_ACQUISTO_CONSUMABILE,
    /**
     * Richiesta di interazione con un commerciante col quale si può fare una compravendita (ad es., l'Armaiolo)
     */
    COMANDO_APERTURA_INVENTARIO_COMMERCIANTE,
    /**
     * Richiesta di interazione con un fornitore col quale si può fare un acquisto (ad es., l'Alchimista)
     */
    COMANDO_APERTURA_INVENTARIO_FORNITORE,
    /**
     * Richiesta di interazione con l'inventario di gruppo
     */
    COMANDO_APERTURA_INVENTARIO_GRUPPO,
    /**
     * Richiesta di interazione con l'incantatore: inventario del gruppo e banco di lavoro per la fusione
     */
    COMANDO_APERTURA_INCANTATORE,
    /**
     * Richiesta di fondere sull'artefatto del banco di lavoro le pergamene che vi stanno
     */
    COMANDO_INCANTATURA,
    /**
     * Il giocatore invia un comando di gioco (generico) all'automa
     */
    COMANDO_DI_GIOCO,
    /**
     * Il giocatore invia un testo al motore
     */
    COMANDO_INVIO_TESTO,
    /**
     * Richiesta di spostamento di un Artefatto dall'inventario del gruppo a un Personaggio
     */
    COMANDO_PRELIEVO_ARTEFATTO,
    /**
     * Richiesta di spostamento di un Artefatto da un Personaggio all'inventario del gruppo
     */
    COMANDO_STOCCAGGIO_ARTEFATTO,
    /**
     * Richiesta di vendita di un Artefatto dall'inventario del gruppo a n commerciante
     */
    COMANDO_VENDITA_ARTEFATTO,
    /**
     * Richiede di visualizzare la mappa conosciuta della foresta a schermo intero
     */
    COMANDO_VISUALIZZAZIONE_MAPPA,


    // Richieste che il motore può fare ad un giocatore
    /**
     * Richiesta di scelta della direzione da seguire
     */
    RICHIESTA_SELEZIONE_DIREZIONE,
    /**
     * Richiesta di scelta di un incantesimo da lanciare
     */
    RICHIESTA_SELEZIONE_INCANTESIMO_DA_LANCIARE,
    /**
     * Richiesta di selezione si o no
     */
    RICHIESTA_SELEZIONE_SI_O_NO,
    /**
     * Richiesta di selezione di uno slot per effettuare una rilettura
     */
    RICHIESTA_SELEZIONE_SLOT_PER_RILETTURA,
    /**
     * Richiesta di selezione di uno slot per effettuare un salvataggio
     */
    RICHIESTA_SELEZIONE_SLOT_PER_SALVATAGGIO,
    /**
     * Il gioco chiede un testo (ad esempio il nome del personaggio)
     */
    RICHIESTA_TESTO,
    /**
     * Il motore chiede conferma per l'uscita dal gioco
     */
    RICHIESTA_USCITA_DAL_GIOCO,


    // Notifiche dal motore al giocatore riguardanti l'avanzamento del gioco
    /**
     * Notifica sull'aggiornamento di uno stato missione
     */
    NOTIFICA_AGGIORNAMENTO_STATO_MISSIONE,
    /**
     * Un Personaggio acquisisce un Modificatore che ne cambia le statistiche
     */
    NOTIFICA_AGGIUNTA_MODIFICATORE_PERSONAGGIO,
    /**
     * Notifica sull'approvazione di una richiesta di acquisto di un Artefatto
     */
    NOTIFICA_APPROVAZIONE_ACQUISTO_ARTEFATTO,
    /**
     * Notifica sull'approvazione di una richiesta di acquisto di un Consumabile
     */
    NOTIFICA_APPROVAZIONE_ACQUISTO_CONSUMABILE,
    /**
     * Notifica sull'approvazione di una richiesta di prelievo di un Artefatto
     */
    NOTIFICA_APPROVAZIONE_PRELIEVO_ARTEFATTO,
    /**
     * Notifica sull'approvazione di una richiesta di stoccaggio di un Artefatto
     */
    NOTIFICA_APPROVAZIONE_STOCCAGGIO_ARTEFATTO,
    /**
     * Notifica sull'approvazione di una richiesta di vendita di un Artefatto
     */
    NOTIFICA_APPROVAZIONE_VENDITA_ARTEFATTO,
    /**
     * Il gioco aumenta di difficoltà
     */
    NOTIFICA_AUMENTO_LIVELLO_MONDO,
    /**
     * Un Personaggio aumenta di livello
     */
    NOTIFICA_AUMENTO_LIVELLO_PERSONAGGIO,
    /**
     * Un Personaggio consuma un punto di abilità per aumentare uno dei suoi attributi primari
     */
    NOTIFICA_CONSUMO_PUNTO_ABILITA_PERSONAGGIO,
    /**
     * Errore di caricamento del gioco
     */
    NOTIFICA_ERRORE_CARICAMENTO,
    /**
     * Fine della partita
     */
    NOTIFICA_FINE_GIOCO,
    /**
     * Mostra un annuncio in evidenza (ad esempio l'inizio di una missione)
     */
    NOTIFICA_GLOBALE,
    /**
     * Un Personaggio inizia a combattere con un altro Personaggio
     */
    NOTIFICA_INIZIO_COMBATTIMENTO_PERSONAGGIO,
    /**
     * Un Personaggio subisce una interazione elementale come effetto collaterale di un combattimento
     */
    NOTIFICA_INTERAZIONE_ELEMENTALE_PERSONAGGIO,
    /**
     * Mostra la finestra con i migliori 10 giocatori di tutti i tempi
     */
    NOTIFICA_MOSTRA_PUNTEGGI_MIGLIORI,
    /**
     * Mostra la finestra con le statistiche sulla partita appena conclusa
     */
    NOTIFICA_MOSTRA_STATISTICHE_FINE_GIOCO,
    /**
     * Raccoglie gli oggetti vinti agli avversari
     */
    NOTIFICA_RACCOLTA_OGGETTI,
    /**
     * Notifica sul rifiuto di una richiesta di acquisto di un Artefatto
     */
    NOTIFICA_RIFIUTO_ACQUISTO_ARTEFATTO,
    /**
     * Notifica sul rifiuto di una richiesta di acquisto di un Consumabile
     */
    NOTIFICA_RIFIUTO_ACQUISTO_CONSUMABILE,
    /**
     * Notifica sul rifiuto di una richiesta di prelievo di un Artefatto
     */
    NOTIFICA_RIFIUTO_PRELIEVO_ARTEFATTO,
    /**
     * Notifica sul rifiuto di una richiesta di stoccaggio di un Artefatto
     */
    NOTIFICA_RIFIUTO_STOCCAGGIO_ARTEFATTO,
    /**
     * Notifica sul rifiuto di una richiesta di vendita di un Artefatto
     */
    NOTIFICA_RIFIUTO_VENDITA_ARTEFATTO,
    /**
     * Notifica di una fusione riuscita dall'incantatore
     */
    NOTIFICA_APPROVAZIONE_INCANTATURA,
    /**
     * Notifica del rifiuto dell'incantatore (sul banco o alla fusione), con il motivo
     */
    NOTIFICA_RIFIUTO_INCANTATURA,
    /**
     * Un messaggio viene inviato dal motore al giocatore. Nuovo paragrafo con spaziatura antecedente
     */
    NOTIFICA_TESTO_PARAGRAFO,
    /**
     * Un messaggio viene inviato dal motore al giocatore. Continuazione del paragrafo precedente
     */
    NOTIFICA_TESTO_FRASE,
    /**
     * Una notizia viene inviata dal motore al giocatore, per essere mostrata in seguito nella schermata della mappa
     */
    NOTIFICA_NOTIZIA,
    /**
     * Una pagina di un intermezzo da mostrare a tutto schermo
     */
    NOTIFICA_PAGINA_INTERMEZZO,
    /**
     * Variazione della mappa conosciuta della foresta
     */
    NOTIFICA_VARIAZIONE_CONOSCENZA_MAPPA,
    /**
     * Variazione delle gemme disponibili al gruppo
     */
    NOTIFICA_VARIAZIONE_DISPONIBILITA_GEMME,
    /**
     * Variazione degli incantesimi disponibili al gruppo
     */
    NOTIFICA_VARIAZIONE_DISPONIBILITA_INCANTESIMI,
    /**
     * Variazione delle monete disponibili al gruppo
     */
    NOTIFICA_VARIAZIONE_DISPONIBILITA_MONETE,
    /**
     * Variazione della quantità di pozioni salute disponibili
     */
    NOTIFICA_VARIAZIONE_DISPONIBILITA_POZIONI_SALUTE,
    /**
     * Variazione della quantità di pozioni salute grandi disponibili
     */
    NOTIFICA_VARIAZIONE_DISPONIBILITA_POZIONI_SALUTE_GRANDI,
    /**
     * Variazione della quantità di pozioni magia disponibili
     */
    NOTIFICA_VARIAZIONE_DISPONIBILITA_POZIONI_MAGIA,
    /**
     * Variazione della quantità di pozioni magia grandi disponibili
     */
    NOTIFICA_VARIAZIONE_DISPONIBILITA_POZIONI_MAGIA_GRANDI,
    /**
     * Un Personaggio subisce una variazione di un effetto di stato come effetto collaterale di un combattimento
     */
    NOTIFICA_VARIAZIONE_EFFETTO_DI_STATO_PERSONAGGIO,
    /**
     * Variazione del punteggio globale
     */
    NOTIFICA_VARIAZIONE_PUNTEGGIO,
    /**
     * Variazione dei punti esperienza di un singolo Personaggio
     */
    NOTIFICA_VARIAZIONE_PUNTI_ESPERIENZA_PERSONAGGIO,
    /**
     * Un Personaggio subisce una variazione delle sue statistiche
     */
    NOTIFICA_VARIAZIONE_STATISTICHE_PERSONAGGIO,
    /**
     * Un Personaggio muore o resuscita
     */
    NOTIFICA_VARIAZIONE_STATO_VITALE_PERSONAGGIO,


    // Eventi interni per il funzionamento del gioco
    /**
     * Elenco dei possibili comandi che l'interfaccia deve mostrare
     */
    INTERNO_AGGIORNAMENTO_COMANDI_DISPONIBILI,
    /**
     * Un salvataggio è stato riletto con successo: porta gli ultimi messaggi da ripristinare nel pannello di testo
     */
    INTERNO_CARICAMENTO_COMPLETATO,
    /**
     * Un nuovo Personaggio viene creato dal motore
     */
    INTERNO_CREAZIONE_PERSONAGGIO,
    /**
     * Un componente interno crea uno sprite di "annuncio globale" e lo notifica al gestore grafico
     */
    INTERNO_CREAZIONE_SPRITE_ANNUNCIO_GLOBALE,
    /**
     * Un componente interno crea uno SpriteATempo e lo notifica al gestore grafico
     */
    INTERNO_CREAZIONE_SPRITE_A_TEMPO,
    /**
     * Un componente interno crea uno SpriteEffettoDiStato e lo notifica al gestore grafico
     */
    INTERNO_CREAZIONE_SPRITE_EFFETTO_DI_STATO,
    /**
     * Un componente interno crea uno SpriteFumettoATempo e lo notifica al gestore grafico
     */
    INTERNO_CREAZIONE_SPRITE_FUMETTO_A_TEMPO,
    /**
     * Un componente interno crea uno SpriteInDissolvenza e lo notifica al gestore grafico
     */
    INTERNO_CREAZIONE_SPRITE_IN_DISSOLVENZA,
    /**
     * Errore interno del motore
     */
    INTERNO_ERRORE,
    /**
     * Eccezione sollevata all'interno del motore
     */
    INTERNO_ECCEZIONE,
    /**
     * Interfaccia utente inizializzata - il sistema è pronto per il gioco
     */
    INTERNO_INTERFACCIA_UTENTE_PRONTA,
    /**
     * L'animazione del logo iniziale e' finita e la UI ha caricato le sue risorse
     */
    INTERNO_FINE_LOGO_INIZIALE,
    /**
     * Il motore ha finito di caricare in background grammatiche e generatore di artefatti
     */
    INTERNO_PRECARICAMENTO_MOTORE_COMPLETATO,
    /**
     * Messaggi di notifica interni al motore non destinati al giocatore
     */
    INTERNO_MESSAGGIO,
    /**
     * Il motore chiede alla UI di mostrare la finestra principale di gioco
     */
    INTERNO_MOSTRA_SCHERMATA_GIOCO,
    /**
     * Chiede di effettuare una notifica al giocatore via fumetto a tempo a video invece che come messaggio
     */
    INTERNO_NOTIFICA_VIA_FUMETTO_A_TEMPO,
    /**
     * Il motore chiede alla UI di portare in primo piano una certa Finestra
     */
    INTERNO_PORTA_IN_PRIMO_PIANO,
    /**
     * Prepara la locazione corrente per il turno di gioco
     */
    INTERNO_PREPARAZIONE_LOCAZIONE,
    /**
     * Attività interna di pulizia cache dinamica immagini
     */
    INTERNO_PULIZIA_CACHE_DINAMICA_IMMAGINI,
    /**
     * Chiede alla UI un refresh
     */
    INTERNO_RICHIESTA_REFRESH_UI,
    /**
     * Richiede una reinizializzazione dell'interfaccia grafica
     */
    INTERNO_RICHIESTA_REINIZIALIZZAZIONE_UI,
    /**
     * Un PNG prende una decisione riguardante un combattimento
     */
    INTERNO_RISULTATO_VALUTAZIONE_PERSONAGGIO_ATTACCANTE,
    /**
     * Mostra al giocatore o chiude la finestra del combattimento
     */
    INTERNO_STATO_FINESTRA_COMBATTIMENTO,
    /**
     * Cambio di stato dell'automa principale che informa la UI
     */
    INTERNO_STATO_DI_GIOCO

}
