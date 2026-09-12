package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConPeso;

/**
 *
 * @author Stefano Reksten
 */
public class EventoApprovazioneStoccaggio extends EventoApprovazioneSpostamento<OggettoConPeso> {

    public EventoApprovazioneStoccaggio(EventoRichiestaStoccaggio eventoRichiestaStoccaggio) {
        super(eventoRichiestaStoccaggio);
    }
}
