package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConCosto;

/**
 * Il GruppoGiocatore (che "fa da banchiere") rifiuta la vendita di un artefatto a un commerciante
 * (per mancanza di fondi da parte del commerciante). Il discriminante è il costo dell'oggetto.
 * <p>
 * NOTA: in questo momento i commercianti hanno fondi infiniti, per cui questo evento non può verificarsi.
 *
 * @author Stefano Reksten
 */
public class EventoRifiutoVenditaArtefatto extends EventoRifiutoSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param eventoRichiestaVenditaArtefatto la richiesta di vendita di un Artefatto che si rifiuta
     */
    public EventoRifiutoVenditaArtefatto(EventoRichiestaVenditaArtefatto eventoRichiestaVenditaArtefatto) {
        super(eventoRichiestaVenditaArtefatto);
    }
}
