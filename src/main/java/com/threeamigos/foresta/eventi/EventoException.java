package com.threeamigos.foresta.eventi;

/**
 * Gestisce una eccezione.
 *
 * @author Stefano Reksten
 */
public class EventoException extends EventoBase {

    private final String messaggio;
    private final Exception exception;

    public EventoException(String messaggio, Exception exception) {
        super(TipoEvento.ERRORE_INTERNO);
        this.messaggio = messaggio;
        this.exception = exception;
    }

    public EventoException(Exception exception) {
        super(TipoEvento.ERRORE_INTERNO);
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
