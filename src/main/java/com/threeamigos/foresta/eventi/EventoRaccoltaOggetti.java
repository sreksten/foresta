package com.threeamigos.foresta.eventi;

/**
 * Il gruppo del giocatore raccoglie gli oggetti vinti agli avversari
 *
 * @author Stefano Reksten
 */
public class EventoRaccoltaOggetti extends EventoBase {

    public EventoRaccoltaOggetti() {
        super(TipoEvento.RACCOLTA_OGGETTI);
    }
}
