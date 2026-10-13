package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Chiede alla UI di mostrare la schermata principale di gioco.
 *
 * @author Stefano Reksten
 */
public class InternoMostraSchermataGioco extends EventoBase {

    public InternoMostraSchermataGioco() {
        super(TipoEvento.INTERNO_MOSTRA_SCHERMATA_GIOCO);
    }
}
