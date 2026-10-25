package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Chiede al giocatore se vuole davvero uscire dal gioco.
 *
 * @author Stefano Reksten
 */
public class RichiestaUscitaDalGioco extends RichiestaSelezioneSiNo {

    public RichiestaUscitaDalGioco() {
        super(TipoEvento.RICHIESTA_USCITA_DAL_GIOCO);
    }

}
