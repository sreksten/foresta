package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Il motore informa il giocatore che il livello generale del gioco è stato aumentato.
 *
 * @author Stefano Reksten
 */
public class NotificaAumentoLivelloMondo extends EventoBase {

    private final int livello;

    public NotificaAumentoLivelloMondo(int livello) {
        super(TipoEvento.NOTIFICA_AUMENTO_LIVELLO_MONDO);
        this.livello = livello;
    }

    public int getLivello() {
        return livello;
    }
}
