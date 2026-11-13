package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Un errore imprevisto (un'eccezione) avvenuto mentre il gioco faceva qualcosa: chi lo prende lo pubblica sul bus, e la
 * UI lo dice al giocatore (vedi {@code SegnalatoreDegliErrori}). Lo pubblica anche il bus stesso, quando un iscritto
 * fallisce durante una consegna.
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
     * Il messaggio da mostrare: quello dato, o quello dell'eccezione; null se non c'è né l'uno né l'altro (molte
     * eccezioni, come una NullPointerException, non hanno messaggio).
     */
    public String getMessaggio() {
        if (messaggio != null) {
            return messaggio;
        }
        return exception == null ? null : exception.getMessage();
    }

    /**
     * L'eccezione che ha causato l'errore.
     */
    public Exception getException() {
        return exception;
    }

}
