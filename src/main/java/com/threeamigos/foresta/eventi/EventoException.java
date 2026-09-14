package com.threeamigos.foresta.eventi;

/**
 * Gestisce una eccezione
 * @author Stefano Reksten
 */
public class EventoException extends EventoBase {

    private final Exception exception;

    public EventoException(Exception exception) {
        super(TipoEvento.ERRORE_INTERNO);
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
