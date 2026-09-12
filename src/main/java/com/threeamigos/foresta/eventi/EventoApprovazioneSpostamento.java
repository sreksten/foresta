package com.threeamigos.foresta.eventi;

/**
 *
 * @author Stefano Reksten
 */
public class EventoApprovazioneSpostamento<T> extends EventoBase {

    private final EventoRichiestaSpostamento<T> eventoRichiestaSpostamento;

    public EventoApprovazioneSpostamento(EventoRichiestaSpostamento<T> eventoRichiestaSpostamento) {
        super(TipoEvento.APPROVAZIONE_SPOSTAMENTO_OGGETTO);
        this.eventoRichiestaSpostamento = eventoRichiestaSpostamento;
    }

    public EventoRichiestaSpostamento<T> getEventoRichiestaSpostamento() {
        return eventoRichiestaSpostamento;
    }
}
