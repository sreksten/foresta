package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * La UI ha finito di mostrare il logo iniziale e di caricare le sue risorse: per lei si puo' passare all'INTRO.
 */
public class InternoFineLogoIniziale extends EventoBase {

    public InternoFineLogoIniziale() {
        super(TipoEvento.INTERNO_FINE_LOGO_INIZIALE);
    }
}
