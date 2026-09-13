package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConCosto;

/**
 * Il GruppoGiocatore (che "fa da banchiere") approva la vendita di un artefatto a un commerciante.
 *
 * @author Stefano Reksten
 */
public class EventoApprovazioneVenditaArtefatto extends EventoApprovazioneSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param eventoRichiestaVenditaArtefatto la richiesta di vendita di un Artefatto che si approva
     */
    public EventoApprovazioneVenditaArtefatto(EventoRichiestaVenditaArtefatto eventoRichiestaVenditaArtefatto) {
        super(eventoRichiestaVenditaArtefatto);
    }
}
