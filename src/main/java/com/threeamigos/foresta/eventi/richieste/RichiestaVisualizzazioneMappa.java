package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il giocatore richiede di poter vedere la mappa del gioco conosciuta a tutto schermo.
 *
 * @author Stefano Reksten
 */
public class RichiestaVisualizzazioneMappa extends EventoBase {

    public RichiestaVisualizzazioneMappa() {
        super(TipoEvento.RICHIESTA_VISUALIZZAZIONE_MAPPA);
    }
}
