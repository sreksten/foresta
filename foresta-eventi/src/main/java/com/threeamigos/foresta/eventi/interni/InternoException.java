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

    /**
     * Un errore con un messaggio che dice che cosa si stava facendo.
     */
    public InternoException(String messaggio, Exception exception) {
        super(TipoEvento.INTERNO_ECCEZIONE);
        this.messaggio = messaggio;
        this.exception = exception;
    }

    /**
     * Un errore senza altro messaggio che quello dell'eccezione.
     */
    public InternoException(Exception exception) {
        super(TipoEvento.INTERNO_ECCEZIONE);
        this.messaggio = null;
        this.exception = exception;
    }

    /**
     * Il messaggio da mostrare: quello dato, o quello dell'eccezione.
     */
    public String getMessaggio() {
        return messaggio != null ? messaggio : exception.getMessage();
    }

    /**
     * L'eccezione che ha causato l'errore.
     */
    public Exception getException() {
        return exception;
    }

}
