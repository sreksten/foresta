package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConCosto;

/**
 *
 * @author Stefano Reksten
 */
public class EventoRifiutoVendita extends EventoRifiutoSpostamento<OggettoConCosto> {

    public EventoRifiutoVendita(EventoRichiestaVendita eventoRichiestaVendita) {
        super(eventoRichiestaVendita);
    }
}
