package com.threeamigos.foresta.eventi;

/**
 *
 * @author Stefano Reksten
 */
public enum TipoEvento {

    PARAGRAFO,
    MESSAGGIO,
    FUMETTO,
    NOTIFICA_GLOBALE,

    RICHIESTA_SPOSTAMENTO_OGGETTO,
    APPROVAZIONE_SPOSTAMENTO_OGGETTO,
    RIFIUTO_SPOSTAMENTO_OGGETTO,

    RICHIESTA_ACQUISTO_CONSUMABILE,
    APPROVAZIONE_ACQUISTO_CONSUMABILE,
    RIFIUTO_ACQUISTO_CONSUMABILE,

    PERSONAGGIO_CREAZIONE,
    PERSONAGGIO_VARIAZIONE_STATO_VITALE,
    PERSONAGGIO_VARIAZIONE_STATISTICHE,
    PERSONAGGIO_VARIAZIONE_EFFETTO_DI_STATO,
    PERSONAGGIO_AGGIUNTA_MODIFICATORE,
    PERSONAGGIO_CONSUMO_PUNTO_ABILITA,
    PERSONAGGIO_COMBATTIMENTO,
    PERSONAGGIO_INTERAZIONE_ELEMENTALE,

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

    // Eventi interni per il funzionamento del gioco

    /**
     * Cambio di stato dell'automa principale
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
    ERRORE_INTERNO

}
