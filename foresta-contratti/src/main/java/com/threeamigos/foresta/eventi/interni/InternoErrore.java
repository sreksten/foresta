package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Un errore interno del programma.
 *
 * @author Stefano Reksten
 */
public class InternoErrore extends EventoBase {

    public final String messaggio;

    public InternoErrore(String messaggio) {
        super(TipoEvento.INTERNO_ERRORE);
        this.messaggio = messaggio;
    }

    public String getMessaggio() {
        return messaggio;
    }
}
