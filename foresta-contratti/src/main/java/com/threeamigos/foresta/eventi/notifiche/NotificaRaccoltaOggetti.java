package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il gruppo del giocatore raccoglie gli oggetti vinti agli avversari
 *
 * @author Stefano Reksten
 */
public class NotificaRaccoltaOggetti extends EventoBase {

    public NotificaRaccoltaOggetti() {
        super(TipoEvento.NOTIFICA_RACCOLTA_OGGETTI);
    }
}
