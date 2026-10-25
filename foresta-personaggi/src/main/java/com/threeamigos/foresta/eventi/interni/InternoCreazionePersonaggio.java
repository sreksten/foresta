package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Evento interno del motore che traccia la creazione di un Personaggio
 *
 * @author Stefano Reksten
 */
public class InternoCreazionePersonaggio extends EventoSuPersonaggio {

    /**
     * @param personaggio il Personaggio appena creato
     */
    public InternoCreazionePersonaggio(Personaggio personaggio) {
        super(TipoEvento.INTERNO_CREAZIONE_PERSONAGGIO, personaggio);
    }
}
