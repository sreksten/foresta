package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * È stato creato un Personaggio: lo pubblica PersonaggioBase alla fine del costruttore. Oggi lo ascolta solo il registro
 * degli eventi di prova (SnifferBusEventi)
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
