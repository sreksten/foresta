package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConPeso;

/**
 * Il GruppoGiocatore approva lo spostamento di un Artefatto dall'inventario generale verso un Personaggio.
 * Il discriminante è il peso dell'oggetto.
 *
 * @author Stefano Reksten
 */
public class EventoApprovazionePrelievoArtefatto extends EventoApprovazioneSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param eventoRichiestaPrelievoArtefatto la richiesta di prelievo di un Artefatto che si approva
     */
    public EventoApprovazionePrelievoArtefatto(EventoRichiestaPrelievoArtefatto eventoRichiestaPrelievoArtefatto) {
        super(eventoRichiestaPrelievoArtefatto);
    }
}
