package com.threeamigos.foresta.eventi;

/**
 * Un errore interno del programma.
 *
 * @author Stefano Reksten
 */
public class EventoErroreInterno extends EventoBase {

    public final String messaggio;

    public EventoErroreInterno(String messaggio) {
        super(TipoEvento.ERRORE_INTERNO);
        this.messaggio = messaggio;
    }

    public String getMessaggio() {
        return messaggio;
    }
}
