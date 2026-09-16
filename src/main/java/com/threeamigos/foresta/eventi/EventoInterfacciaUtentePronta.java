package com.threeamigos.foresta.eventi;

/**
 * Evento iniziale che annuncia il completamento del caricamento del gioco e della preparazione
 * della UI. Il gioco può iniziare.
 *
 * @author Stefano Reksten
 */
public class EventoInterfacciaUtentePronta extends EventoBase {

    public EventoInterfacciaUtentePronta() {
        super(TipoEvento.INTERFACCIA_UTENTE_PRONTA);
    }
}
