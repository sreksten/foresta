package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.modellodati.TatticaMD;

/**
 * Il giocatore imposta, dalla schermata delle tattiche, la tattica di un personaggio del gruppo. La tattica è un dato
 * puro e immutabile: la UI la costruisce a parte e la manda finita.
 */
public class ComandoImpostaTattica extends EventoBase {

    private final String uuidPersonaggio;
    private final TatticaMD tattica;

    public ComandoImpostaTattica(String uuidPersonaggio, TatticaMD tattica) {
        super(TipoEvento.COMANDO_IMPOSTA_TATTICA);
        this.uuidPersonaggio = uuidPersonaggio;
        this.tattica = tattica;
    }

    public String getUuidPersonaggio() {
        return uuidPersonaggio;
    }

    public TatticaMD getTattica() {
        return tattica;
    }

}
