package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConCosto;

/**
 * Il GruppoGiocatore (che "fa da banchiere") approva l'acquisto di un Artefatto da un commerciante.
 * Il discriminante è il costo dell'oggetto.
 *
 * @author Stefano Reksten
 */
public class EventoApprovazioneAcquistoArtefatto extends EventoApprovazioneSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param eventoRichiestaAcquistoArtefatto la richiesta di acquisto di un Artefatto che si approva
     */
    public EventoApprovazioneAcquistoArtefatto(EventoRichiestaAcquistoArtefatto eventoRichiestaAcquistoArtefatto) {
        super(eventoRichiestaAcquistoArtefatto);
    }
}
