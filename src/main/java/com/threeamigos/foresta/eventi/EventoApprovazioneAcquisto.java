package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConCosto;

/**
 *
 * @author Stefano Reksten
 */
public class EventoApprovazioneAcquisto extends EventoApprovazioneSpostamento<OggettoConCosto> {

    public EventoApprovazioneAcquisto(EventoRichiestaAcquisto eventoRichiestaAcquisto) {
        super(eventoRichiestaAcquisto);
    }
}
