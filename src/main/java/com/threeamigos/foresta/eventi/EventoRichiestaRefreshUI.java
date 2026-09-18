package com.threeamigos.foresta.eventi;

/**
 * Chiede un rinfresco della schermata di gioco.
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaRefreshUI extends EventoBase {

    public EventoRichiestaRefreshUI() {
        super(TipoEvento.REFRESH);
    }
}
