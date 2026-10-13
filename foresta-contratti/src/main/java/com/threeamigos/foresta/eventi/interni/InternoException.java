package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Gestisce una eccezione.
 *
 * @author Stefano Reksten
 */
public class InternoException extends EventoBase {

    private final String messaggio;
    private final Exception exception;

    public InternoException(String messaggio, Exception exception) {
        super(TipoEvento.INTERNO_ERRORE);
        this.messaggio = messaggio;
        this.exception = exception;
    }

    public InternoException(Exception exception) {
        super(TipoEvento.INTERNO_ERRORE);
        this.messaggio = null;
        this.exception = exception;
    }

    public String getMessaggio() {
        return messaggio != null ? messaggio : exception.getMessage();
    }

    public Exception getException() {
        return exception;
    }
}
