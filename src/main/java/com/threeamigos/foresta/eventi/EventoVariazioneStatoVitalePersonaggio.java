package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class EventoVariazioneStatoVitalePersonaggio extends EventoPersonaggio {

    public EventoVariazioneStatoVitalePersonaggio(Personaggio personaggio) {
        super(TipoEvento.PERSONAGGIO_VARIAZIONE_STATO_VITALE, personaggio);
    }
}
