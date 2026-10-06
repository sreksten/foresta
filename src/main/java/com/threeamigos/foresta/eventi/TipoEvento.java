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
     * Richiesta di acquisto di un Consumabile da un fornitore
     */
    COMANDO_ACQUISTO_CONSUMABILE,
    /**
     * Il giocatore invia un comando di gioco (generico) all'automa
     */
    COMANDO_DI_GIOCO,
    /**
     * Il giocatore accende o spegne l'aiuto della barra delle icone
     */
    COMANDO_IMPOSTAZIONE_AIUTO,
    /**
     * Il giocatore apre o chiude l'elenco dei modificatori di un artefatto o la descrizione di una missione
     */
    COMANDO_COMMUTAZIONE_ELENCO,
    /**
     * Il giocatore spende un punto abilità di un personaggio
     */
    COMANDO_SPESA_PUNTO_ABILITA,
    /**
     * Il giocatore sposta un artefatto da una parte all'altra di uno scambio aperto
     */
    COMANDO_SCAMBIO_ARTEFATTO,
    /**
     * Il giocatore invia un testo al motore
     */
    COMANDO_INVIO_TESTO,


    // Richieste che il motore può fare ad un giocatore
    /**
     * Richiesta di interazione con l'incantatore: inventario del gruppo e banco di lavoro per la fusione
     */
    RICHIESTA_APERTURA_INCANTATORE,
    /**
     * Richiesta di interazione con un commerciante col quale si può fare una compravendita (ad es., l'Armaiolo)
     */
    RICHIESTA_APERTURA_INVENTARIO_COMMERCIANTE,
    /**
     * Richiesta di interazione con un fornitore col quale si può fare un acquisto (ad es., l'Alchimista)
     */
    RICHIESTA_APERTURA_INVENTARIO_FORNITORE,
    /**
     * Richiesta di interazione con l'inventario di gruppo
     */
    RICHIESTA_APERTURA_INVENTARIO_GRUPPO,
    /**
     * Richiesta di vedere la pagina dei trofei, dall'inventario
     */
    RICHIESTA_APERTURA_TROFEI,
    /**
     * Richiede di visualizzare la mappa conosciuta della foresta a schermo intero
     */
    RICHIESTA_VISUALIZZAZIONE_MAPPA,
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
     * Una missione chiede una conferma o una scelta fra più opzioni
     */
    RICHIESTA_SELEZIONE_MISSIONE,
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
     * Il gruppo ha trovato un artefatto in un cofano o in un tempio
     */
    NOTIFICA_ARTEFATTO_TROVATO,
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
     * Un Personaggio subisce una interazione con un effetto di stato come effetto collaterale di un combattimento
     */
    NOTIFICA_INTERAZIONE_PERSONAGGIO,
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
     * Notifica di un avviso dell'incantatore su quel che c'è sul banco, che non impedisce la fusione
     */
    NOTIFICA_AVVISO_INCANTATURA,
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
     * Variazione delle pietre preziose disponibili al gruppo
     */
    NOTIFICA_VARIAZIONE_DISPONIBILITA_PREZIOSI,
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
     * Richiesta di acquisto di un Consumabile da un commerciante
     */
    INTERNO_ACQUISTO_ARTEFATTO,
    /**
     * Richiesta di fondere sull'artefatto del banco di lavoro le pergamene che vi stanno
     */
    INTERNO_INCANTATURA,
    /**
     * Richiesta di spostamento di un Artefatto dall'inventario del gruppo a un Personaggio
     */
    INTERNO_PRELIEVO_ARTEFATTO,
    /**
     * Richiesta di spostamento di un Artefatto da un Personaggio all'inventario del gruppo
     */
    INTERNO_STOCCAGGIO_ARTEFATTO,
    /**
     * Richiesta di vendita di un Artefatto dall'inventario del gruppo a n commerciante
     */
    INTERNO_VENDITA_ARTEFATTO,
    /**
     * Elenco dei possibili comandi che l'interfaccia deve mostrare
     */
    INTERNO_AGGIORNAMENTO_COMANDI_DISPONIBILI,
    /**
     * Un personaggio del gruppo ha stretto amicizia con il gruppo avversario
     */
    INTERNO_AMICIZIA_STRETTA,
    /**
     * Un avversario del gruppo è stato sconfitto
     */
    INTERNO_AVVERSARIO_SCONFITTO,
    /**
     * Un personaggio si è arreso invece di morire, in un combattimento fino alla resa
     */
    INTERNO_PERSONAGGIO_ARRESO,
    /**
     * Il gruppo è passato inosservato in una locazione, senza combattere
     */
    INTERNO_PASSAGGIO_INOSSERVATO,
    /**
     * Un salvataggio è stato riletto con successo: porta gli ultimi messaggi da ripristinare nel pannello di testo
     */
    INTERNO_CARICAMENTO_COMPLETATO,
    /**
     * Il gruppo ha corrotto il gruppo avversario, ottenendo un passaggio sicuro
     */
    INTERNO_CORRUZIONE_RIUSCITA,
    /**
     * Un fumetto di un intermezzo viene creato dal gestore grafico, con la frase che mostra
     */
    INTERNO_CREAZIONE_FUMETTO,
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
     * Il gruppo lascia la locazione corrente
     */
    INTERNO_FINE_LOCAZIONE,
    /**
     * L'animazione del logo iniziale e' finita e la UI ha caricato le sue risorse
     */
    INTERNO_FINE_LOGO_INIZIALE,
    /**
     * Il motore ha finito di caricare in background grammatiche e generatore di artefatti
     */
    INTERNO_PRECARICAMENTO_MOTORE_COMPLETATO,
    /**
     * La UI ha appena iniziato un'animazione (sprite o annuncio globale) da portare a termine
     * prima che l'Automa possa mostrare il prossimo intermezzo
     */
    INTERNO_UI_OCCUPATA,
    /**
     * La UI non ha piu' sprite attivi ne' annunci globali in coda o in corso
     */
    INTERNO_UI_INATTIVA,
    /**
     * Messaggi di notifica interni al motore non destinati al giocatore
     */
    INTERNO_MESSAGGIO,
    /**
     * Il motore chiede alla UI di mostrare la finestra principale di gioco
     */
    INTERNO_MOSTRA_SCHERMATA_GIOCO,
    /**
     * Il motore chiede alla UI di saltare al fumetto successivo nella pagina dell'intermezzo
     */
    INTERNO_FUMETTO_SUCCESSIVO,
    /**
     * Il motore chiede alla UI di assegnare di nuovo immagini e posizioni ai personaggi in locazione
     */
    INTERNO_ASSEGNA_COORDINATE_A_PERSONAGGI,
    /**
     * Chiede di effettuare una notifica al giocatore via fumetto a tempo a video invece che come messaggio
     */
    INTERNO_NOTIFICA_VIA_FUMETTO_A_TEMPO,
    /**
     * Una missione è stata completata
     */
    INTERNO_MISSIONE_COMPLETATA,
    /**
     * Il gruppo ha raccolto l'oggetto di fine locazione
     */
    INTERNO_OGGETTO_RACCOLTO,
    /**
     * Il gruppo, o almeno uno dei suoi personaggi, ha consumato un pasto in una locanda
     */
    INTERNO_PASTO_CONSUMATO_IN_LOCANDA,
    /**
     * Il motore chiede alla UI di portare in primo piano il riquadro dello stato del gruppo
     */
    INTERNO_MOSTRA_FINESTRA_STATO,
    /**
     * Il motore chiede alla UI di portare in primo piano il riquadro delle statistiche
     */
    INTERNO_MOSTRA_FINESTRA_STATISTICHE,
    /**
     * Il motore chiede alla UI di portare in primo piano il riquadro degli incantesimi e delle pozioni
     */
    INTERNO_MOSTRA_FINESTRA_INCANTESIMI_E_POZIONI,
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
    INTERNO_FASE_DI_GIOCO,
    /**
     * Il giocatore ha vinto un trofeo
     */
    INTERNO_TROFEO_ACQUISITO

}
