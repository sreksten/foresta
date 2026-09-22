package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.modellodati.Notizia;

/**
 * Una notizia che il motore di gioco invia al giocatore, destinata a essere mostrata
 * in seguito nella schermata della mappa (non nel pannello di testo). A differenza
 * di {@link NotificaTestoFrase}/{@link NotificaTestoParagrafo}, porta un
 * identificativo oltre al corpo testuale.
 *
 * @author Stefano Reksten
 */
public class NotificaNotizia extends EventoBase {

    private final Notizia notizia;

    public NotificaNotizia(Notizia notizia) {
        super(TipoEvento.NOTIFICA_NOTIZIA);
        this.notizia = notizia;
    }

    public Notizia getNotizia() {
        return notizia;
    }
}
