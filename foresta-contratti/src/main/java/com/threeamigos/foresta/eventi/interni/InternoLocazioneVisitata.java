package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.TipoLocazione;

/**
 * Il gruppo è arrivato per la prima volta in una casella: tornarci non lo pubblica di nuovo. Chi conta le visite
 * (i trofei) si iscrive qui.
 */
public class InternoLocazioneVisitata extends EventoBase {

    private final TipoLocazione tipo;

    public InternoLocazioneVisitata(TipoLocazione tipo) {
        super(TipoEvento.INTERNO_LOCAZIONE_VISITATA);
        this.tipo = tipo;
    }

    public TipoLocazione getTipo() {
        return tipo;
    }

}
