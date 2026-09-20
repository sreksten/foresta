package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Notifica un cambiamento nella conoscenza della mappa della Foresta.
 *
 * @author Stefano Reksten
 */
public class NotificaVariazioneConoscenzaMappa extends EventoBase {

    private final int daX;
    private final int daY;
    private final int aX;
    private final int aY;

    public NotificaVariazioneConoscenzaMappa(int daX, int daY, int aX, int aY) {
        super(TipoEvento.NOTIFICA_VARIAZIONE_CONOSCENZA_MAPPA);
        this.daX = daX;
        this.daY = daY;
        this.aX = aX;
        this.aY = aY;
    }

    public int getDaX() {
        return daX;
    }

    public int getDaY() {
        return daY;
    }

    public int getaX() {
        return aX;
    }

    public int getaY() {
        return aY;
    }
}
