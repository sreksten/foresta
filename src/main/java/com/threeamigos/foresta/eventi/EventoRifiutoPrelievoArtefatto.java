package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConPeso;

/**
 * Il GruppoGiocatore rifiuta lo spostamento di un Artefatto dall'inventario generale verso un Personaggio
 * (perché già troppo carico). Il discriminante è il peso dell'oggetto.
 *
 * @author Stefano Reksten
 */
public class EventoRifiutoPrelievoArtefatto extends EventoRifiutoSpostamentoArtefatto<OggettoConPeso> {

    /**
     * @param eventoRichiestaPrelievoArtefatto la richiesta di prelievo di un Artefatto che si rifiuta
     */
    public EventoRifiutoPrelievoArtefatto(EventoRichiestaPrelievoArtefatto eventoRichiestaPrelievoArtefatto) {
        super(eventoRichiestaPrelievoArtefatto);
    }
}
