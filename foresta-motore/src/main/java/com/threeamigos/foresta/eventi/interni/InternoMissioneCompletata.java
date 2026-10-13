package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.missioni.Missione;

/**
 * Una missione, di primo livello o secondaria, è stata completata.
 */
public class InternoMissioneCompletata extends EventoBase {

    private final Missione missione;

    public InternoMissioneCompletata(Missione missione) {
        super(TipoEvento.INTERNO_MISSIONE_COMPLETATA);
        this.missione = missione;
    }

    public Missione getMissione() {
        return missione;
    }
}
