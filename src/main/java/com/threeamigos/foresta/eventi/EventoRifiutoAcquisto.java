package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConCosto;

/**
 *
 * @author Stefano Reksten
 */
public class EventoRifiutoAcquisto extends EventoRifiutoSpostamento<OggettoConCosto> {

    public EventoRifiutoAcquisto(EventoRichiestaAcquisto eventoRichiestaAcquisto) {
        super(eventoRichiestaAcquisto);
    }
}
