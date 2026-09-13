package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Evento interno del motore che traccia la creazione di un Personaggio
 *
 * @author Stefano Reksten
 */
public class EventoCreazionePersonaggio extends EventoPersonaggio {

    /**
     * @param personaggio il Personaggio appena creato
     */
    public EventoCreazionePersonaggio(Personaggio personaggio) {
        super(TipoEvento.PERSONAGGIO_CREAZIONE, personaggio);
    }
}
