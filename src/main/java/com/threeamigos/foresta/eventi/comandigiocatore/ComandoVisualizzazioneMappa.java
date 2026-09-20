package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il giocatore richiede di poter vedere la mappa del gioco conosciuta a tutto schermo.
 *
 * @author Stefano Reksten
 */
public class ComandoVisualizzazioneMappa extends EventoBase {

    public ComandoVisualizzazioneMappa() {
        super(TipoEvento.COMANDO_VISUALIZZAZIONE_MAPPA);
    }
}
