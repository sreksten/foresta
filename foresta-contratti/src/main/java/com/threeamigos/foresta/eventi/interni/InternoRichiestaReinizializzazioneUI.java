package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Richiede alla UI di reinizializzare il tutto per l'inizio del gioco
 *
 * @author Stefano Reksten
 */
public class InternoRichiestaReinizializzazioneUI extends EventoBase {

    public InternoRichiestaReinizializzazioneUI() {
        super(TipoEvento.INTERNO_RICHIESTA_REINIZIALIZZAZIONE_UI);
    }

}
