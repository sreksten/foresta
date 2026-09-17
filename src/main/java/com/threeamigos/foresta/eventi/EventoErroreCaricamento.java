package com.threeamigos.foresta.eventi;

/**
 *
 * @author Stefano Reksten
 */
public class EventoErroreCaricamento extends EventoBase {

    private final Exception exception;

    public EventoErroreCaricamento(Exception exception) {
        super(TipoEvento.ERRORE_CARICAMENTO);
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
