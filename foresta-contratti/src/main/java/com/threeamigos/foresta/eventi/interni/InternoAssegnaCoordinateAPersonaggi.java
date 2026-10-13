package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Chiede alla UI di assegnare di nuovo immagini e posizioni ai personaggi in locazione, perché sono cambiati: per
 * esempio, è arrivata un'altra ondata di avversari (vedi GruppoAvversario.prossimaOndata).
 */
public class InternoAssegnaCoordinateAPersonaggi extends EventoBase {

    public InternoAssegnaCoordinateAPersonaggi() {
        super(TipoEvento.INTERNO_ASSEGNA_COORDINATE_A_PERSONAGGI);
    }
}
