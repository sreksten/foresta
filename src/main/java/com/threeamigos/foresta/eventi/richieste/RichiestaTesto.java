package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il gioco richiede un testo al giocatore (ad esempio il nome del personaggio).
 *
 * @author Stefano Reksten
 */
public class RichiestaTesto extends EventoBase {

    private final String richiesta;

    public RichiestaTesto(String richiesta) {
        super(TipoEvento.RICHIESTA_TESTO);
        this.richiesta = richiesta;
    }

    public String getRichiesta() {
        return richiesta;
    }
}
