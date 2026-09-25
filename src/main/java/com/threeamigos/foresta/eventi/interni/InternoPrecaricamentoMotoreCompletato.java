package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il motore ha caricato in background, durante il logo iniziale, cio' che altrimenti caricherebbe al primo uso.
 */
public class InternoPrecaricamentoMotoreCompletato extends EventoBase {

    public InternoPrecaricamentoMotoreCompletato() {
        super(TipoEvento.INTERNO_PRECARICAMENTO_MOTORE_COMPLETATO);
    }
}
