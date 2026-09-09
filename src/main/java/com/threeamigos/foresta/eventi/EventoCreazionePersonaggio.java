package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class EventoCreazionePersonaggio extends EventoPersonaggio {

    public EventoCreazionePersonaggio(Personaggio personaggio) {
        super(TipoEvento.PERSONAGGIO_CREAZIONE, personaggio);
    }
}
