package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il motore chiede alla UI di portare in primo piano il riquadro degli incantesimi e delle pozioni.
 *
 * @author Stefano Reksten
 */
public class InternoMostraFinestraIncantesimiEPozioni extends EventoBase {

    public InternoMostraFinestraIncantesimiEPozioni() {
        super(TipoEvento.INTERNO_MOSTRA_FINESTRA_INCANTESIMI_E_POZIONI);
    }
}
