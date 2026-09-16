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

    VARIAZIONE_DISPONIBILITA_CONSUMABILE,
    VARIAZIONE_GEMME,
    VARIAZIONE_MONETE,
    VARIAZIONE_PUNTI,
    VARIAZIONE_PUNTI_ESPERIENZA,
    VARIAZIONE_INCANTESIMI,
    VARIAZIONE_MAPPA,
    VARIAZIONE_POZIONI_SALUTE,
    VARIAZIONE_POZIONI_SALUTE_GRANDI,
    VARIAZIONE_POZIONI_MAGIA,
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
     * Cambio di stato dell'automa principale
     */
    STATO_DI_GIOCO,
    /**
     * Interfaccia utente inizializzata
     */
    INTERFACCIA_UTENTE_PRONTA,
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
