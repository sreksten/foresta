package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Chiede un rinfresco della schermata di gioco.
 *
 * @author Stefano Reksten
 */
public class InternoRichiestaRefreshUI extends EventoBase {

    public InternoRichiestaRefreshUI() {
        super(TipoEvento.INTERNO_RICHIESTA_REFRESH_UI);
    }
}
