package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConCosto;

/**
 *
 * @author Stefano Reksten
 */
public class EventoApprovazioneVendita extends EventoApprovazioneSpostamento<OggettoConCosto> {

    public EventoApprovazioneVendita(EventoRichiestaVendita eventoRichiestaVendita) {
        super(eventoRichiestaVendita);
    }
}
