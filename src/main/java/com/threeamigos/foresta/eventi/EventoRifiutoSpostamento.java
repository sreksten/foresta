package com.threeamigos.foresta.eventi;

/**
 *
 * @author Stefano Reksten
 */
public class EventoRifiutoSpostamento<T> extends EventoBase {

    private final EventoRichiestaSpostamento<T> eventoRichiestaSpostamento;

    public EventoRifiutoSpostamento(EventoRichiestaSpostamento<T> eventoRichiestaSpostamento) {
        super(TipoEvento.RIFIUTO_SPOSTAMENTO_OGGETTO);
        this.eventoRichiestaSpostamento = eventoRichiestaSpostamento;
    }

    public EventoRichiestaSpostamento<T> getEventoRichiestaSpostamento() {
        return eventoRichiestaSpostamento;
    }
}
