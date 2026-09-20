package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Prepara la locazione per il turno che deve iniziare.
 *
 * @author Stefano Reksten
 */
public class InternoPreparazioneLocazione extends EventoBase {

    public InternoPreparazioneLocazione() {
        super(TipoEvento.INTERNO_PREPARAZIONE_LOCAZIONE);
    }
}
