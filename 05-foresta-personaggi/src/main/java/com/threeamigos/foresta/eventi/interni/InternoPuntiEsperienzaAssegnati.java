package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Il gruppo del giocatore ha guadagnato dei punti esperienza (li ha già dati ai suoi personaggi): chi tiene il conto del
 * livello del mondo (le statistiche) si iscrive qui.
 */
public class InternoPuntiEsperienzaAssegnati extends EventoBase {

    private final int punti;

    /**
     * Il gruppo ha guadagnato quei punti esperienza.
     */
    public InternoPuntiEsperienzaAssegnati(int punti) {
        super(TipoEvento.INTERNO_PUNTI_ESPERIENZA_ASSEGNATI);
        this.punti = punti;
    }

    /**
     * Quanti punti esperienza.
     */
    public int getPunti() {
        return punti;
    }
}
