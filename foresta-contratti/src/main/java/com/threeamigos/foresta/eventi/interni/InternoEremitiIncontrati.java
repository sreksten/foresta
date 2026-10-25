package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Il gruppo ha davanti degli eremiti, appena descritta la locazione: si conta l'incontro, non il suo esito (battaglia,
 * amicizia, corruzione o fuga).
 */
public class InternoEremitiIncontrati extends EventoBase {

    private final int quantita;

    public InternoEremitiIncontrati(int quantita) {
        super(TipoEvento.INTERNO_EREMITI_INCONTRATI);
        this.quantita = quantita;
    }

    public int getQuantita() {
        return quantita;
    }

}
