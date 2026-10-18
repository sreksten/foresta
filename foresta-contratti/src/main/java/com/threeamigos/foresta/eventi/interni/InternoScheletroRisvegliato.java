package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Un avversario mietuto si è risvegliato come scheletro ed è passato dalla parte del gruppo.
 */
public class InternoScheletroRisvegliato extends EventoBase {

    public InternoScheletroRisvegliato() {
        super(TipoEvento.INTERNO_SCHELETRO_RISVEGLIATO);
    }

}
