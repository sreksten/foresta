package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.tipi.TipoTrofeo;

/**
 * Il giocatore ha appena vinto un trofeo: la UI lo annuncia con una notifica globale.
 */
public class InternoTrofeoAcquisito extends EventoBase {

    private final TipoTrofeo trofeo;

    public InternoTrofeoAcquisito(TipoTrofeo trofeo) {
        super(TipoEvento.INTERNO_TROFEO_ACQUISITO);
        this.trofeo = trofeo;
    }

    public TipoTrofeo getTrofeo() {
        return trofeo;
    }
}
