package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Notifica un cambiamento nella conoscenza della mappa della Foresta: il gruppo ora conosce l'area rettangolare che va
 * dalla casella (daX, daY) alla casella (aX, aY), per esempio perché ha comprato o ricevuto una mappa.
 *
 * @author Stefano Reksten
 */
public class NotificaVariazioneConoscenzaMappa extends EventoBase {

    private final int daX;
    private final int daY;
    private final int aX;
    private final int aY;

    /**
     * Il gruppo ha scoperto le caselle di quella zona della mappa.
     */
    public NotificaVariazioneConoscenzaMappa(int daX, int daY, int aX, int aY) {
        super(TipoEvento.NOTIFICA_VARIAZIONE_CONOSCENZA_MAPPA);
        this.daX = daX;
        this.daY = daY;
        this.aX = aX;
        this.aY = aY;
    }

    /**
     * La prima colonna della zona.
     */
    public int getDaX() {
        return daX;
    }

    /**
     * La prima riga della zona.
     */
    public int getDaY() {
        return daY;
    }

    /**
     * L'ultima colonna della zona.
     */
    public int getAX() {
        return aX;
    }

    /**
     * L'ultima riga della zona.
     */
    public int getAY() {
        return aY;
    }

}
