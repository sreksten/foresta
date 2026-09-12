package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConPeso;

/**
 *
 * @author Stefano Reksten
 */
public class EventoRifiutoPrelievo extends EventoRifiutoSpostamento<OggettoConPeso> {

    public EventoRifiutoPrelievo(EventoRichiestaPrelievo eventoRichiestaPrelievo) {
        super(eventoRichiestaPrelievo);
    }
}
