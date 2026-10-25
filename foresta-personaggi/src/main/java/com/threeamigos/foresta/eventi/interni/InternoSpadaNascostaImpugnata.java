package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Un personaggio ha equipaggiato la spada normalmente nascosta (vedi SpadaNormalmenteNascosta): lo conta il trofeo Trovarobe.
 */
public class InternoSpadaNascostaImpugnata extends EventoBase {

    public InternoSpadaNascostaImpugnata() {
        super(TipoEvento.INTERNO_SPADA_NASCOSTA_IMPUGNATA);
    }
}
