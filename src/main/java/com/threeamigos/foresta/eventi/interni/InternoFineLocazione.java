package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il gruppo lascia la locazione corrente, completa o meno: missioni ed eventuale locazione
 * sono già state aggiornate. È il momento in cui si controllano i trofei.
 */
public class InternoFineLocazione extends EventoBase {

    public InternoFineLocazione() {
        super(TipoEvento.INTERNO_FINE_LOCAZIONE);
    }
}
