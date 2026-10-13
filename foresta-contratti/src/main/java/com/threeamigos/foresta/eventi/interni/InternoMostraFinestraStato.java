package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il motore chiede alla UI di portare in primo piano il riquadro dello stato del gruppo.
 *
 * @author Stefano Reksten
 */
public class InternoMostraFinestraStato extends EventoBase {

    public InternoMostraFinestraStato() {
        super(TipoEvento.INTERNO_MOSTRA_FINESTRA_STATO);
    }
}
