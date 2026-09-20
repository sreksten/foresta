package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 *
 * @author Stefano Reksten
 */
public class NotificaErroreCaricamento extends EventoBase {

    private final Exception exception;

    public NotificaErroreCaricamento(Exception exception) {
        super(TipoEvento.NOTIFICA_ERRORE_CARICAMENTO);
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
