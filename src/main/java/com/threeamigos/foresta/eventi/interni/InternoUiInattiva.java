package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * La UI non ha piu' sprite attivi ne' annunci globali in coda o in corso.
 */
public class InternoUiInattiva extends EventoBase {

    public InternoUiInattiva() {
        super(TipoEvento.INTERNO_UI_INATTIVA);
    }
}
