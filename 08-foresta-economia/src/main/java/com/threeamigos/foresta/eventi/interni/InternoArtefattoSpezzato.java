package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * L'incantatore ha spezzato un artefatto nell'artefatto base e negli ingredienti "del Core Dump", uno per incantamento (vedi RegoleIncantatura.riprogramma:
 * succede quando sul banco c'è il libro che riprogramma): lo conta il trofeo Hacker.
 */
public class InternoArtefattoSpezzato extends EventoBase {

    public InternoArtefattoSpezzato() {
        super(TipoEvento.INTERNO_ARTEFATTO_SPEZZATO);
    }
}
