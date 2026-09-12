package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConPeso;

/**
 *
 * @author Stefano Reksten
 */
public class EventoRifiutoStoccaggio extends EventoRifiutoSpostamento<OggettoConPeso> {

    public EventoRifiutoStoccaggio(EventoRichiestaStoccaggio eventoRichiestaStoccaggio) {
        super(eventoRichiestaStoccaggio);
    }
}
