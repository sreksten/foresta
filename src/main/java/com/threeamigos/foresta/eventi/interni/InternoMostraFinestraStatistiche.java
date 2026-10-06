package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il motore chiede alla UI di portare in primo piano il riquadro delle statistiche.
 *
 * @author Stefano Reksten
 */
public class InternoMostraFinestraStatistiche extends EventoBase {

    public InternoMostraFinestraStatistiche() {
        super(TipoEvento.INTERNO_MOSTRA_FINESTRA_STATISTICHE);
    }
}
