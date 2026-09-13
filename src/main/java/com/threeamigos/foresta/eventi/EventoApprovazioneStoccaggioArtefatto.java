package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConPeso;

/**
 * Il GruppoGiocatore approva lo spostamento di un Artefatto da un Personaggio verso l'inventario generale.
 *
 * @author Stefano Reksten
 */
public class EventoApprovazioneStoccaggioArtefatto extends EventoApprovazioneSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param eventoRichiestaStoccaggioArtefatto la richiesta di stoccaggio di un Artefatto che si approva
     */
    public EventoApprovazioneStoccaggioArtefatto(EventoRichiestaStoccaggioArtefatto eventoRichiestaStoccaggioArtefatto) {
        super(eventoRichiestaStoccaggioArtefatto);
    }
}
