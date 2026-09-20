package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.Comando;

import java.util.Collection;

/**
 *
 * @author Stefano Reksten
 */
public class EventoSelezioneDirezione extends EventoConComandi {

    public EventoSelezioneDirezione(Collection<Comando> possibilita) {
        super(TipoEvento.RICHIESTA_SELEZIONE_DIREZIONE, possibilita);
    }
}
