package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il giocatore invia un testo al motore.
 *
 * @author Stefano Reksten
 */
public class ComandoInvioTesto extends EventoBase {

    private final String testo;

    public ComandoInvioTesto(String testo) {
        super(TipoEvento.COMANDO_INVIO_TESTO);
        this.testo = testo;
    }

    public String getTesto() {
        return testo;
    }
}
