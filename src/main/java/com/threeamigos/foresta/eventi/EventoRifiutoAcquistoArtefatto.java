package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConCosto;

/**
 * Il GruppoGiocatore (che "fa da banchiere") rifiuta l'acquisto di un artefatto da un commerciante
 * (per mancanza di fondi da parte del giocatore). Il discriminante è sul costo dell'oggetto.
 *
 * @author Stefano Reksten
 */
public class EventoRifiutoAcquistoArtefatto extends EventoRifiutoSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param eventoRichiestaAcquistoArtefatto la richiesta di acquisto di un Artefatto che si rifiuta
     */
    public EventoRifiutoAcquistoArtefatto(EventoRichiestaAcquistoArtefatto eventoRichiestaAcquistoArtefatto) {
        super(eventoRichiestaAcquistoArtefatto);
    }
}
