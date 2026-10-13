package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * La UI ha appena iniziato qualcosa (uno sprite, un annuncio globale) che deve finire di
 * animarsi prima che l'Automa possa mostrare il prossimo intermezzo.
 */
public class InternoUiOccupata extends EventoBase {

    public InternoUiOccupata() {
        super(TipoEvento.INTERNO_UI_OCCUPATA);
    }
}
