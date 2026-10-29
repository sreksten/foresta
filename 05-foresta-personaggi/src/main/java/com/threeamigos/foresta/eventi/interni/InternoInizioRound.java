package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Comincia un round di combattimento (vedi StatoDelCombattimento): un giro completo dei personaggi dei due gruppi. Il
 * primo round (il numero 1) comincia a ogni inizio locazione.
 */
public class InternoInizioRound extends EventoBase {

    private final int numeroRound;

    /**
     * @param numeroRound il numero del round che comincia, a partire da 1
     */
    public InternoInizioRound(int numeroRound) {
        super(TipoEvento.INTERNO_INIZIO_ROUND);
        this.numeroRound = numeroRound;
    }

    public int getNumeroRound() {
        return numeroRound;
    }

}
