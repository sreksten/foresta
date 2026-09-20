package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 *
 * @author Stefano Reksten
 */
public class EventoErroreCaricamento extends EventoBase {

    private final Exception exception;

    public EventoErroreCaricamento(Exception exception) {
        super(TipoEvento.NOTIFICA_ERRORE_CARICAMENTO);
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
