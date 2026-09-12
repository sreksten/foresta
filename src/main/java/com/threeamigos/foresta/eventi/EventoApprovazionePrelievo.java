package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConPeso;

/**
 *
 * @author Stefano Reksten
 */
public class EventoApprovazionePrelievo extends EventoApprovazioneSpostamento<OggettoConPeso> {

    public EventoApprovazionePrelievo(EventoRichiestaPrelievo eventoRichiestaPrelievo) {
        super(eventoRichiestaPrelievo);
    }
}
