package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Una notizia che il motore di gioco invia al giocatore, destinata a essere mostrata
 * in seguito nella schermata della mappa (non nel pannello di testo). A differenza
 * di {@link NotificaTestoFrase}/{@link NotificaTestoParagrafo}, porta un
 * identificativo oltre al corpo testuale.
 *
 * @author Stefano Reksten
 */
public class NotificaNotizia extends EventoBase {

    private final String id;
    private final String corpo;

    /**
     * @param id    identificativo della notizia
     * @param corpo corpo della notizia
     */
    public NotificaNotizia(String id, String corpo) {
        super(TipoEvento.NOTIFICA_NOTIZIA);
        this.id = id;
        this.corpo = corpo;
    }

    public String getId() {
        return id;
    }

    public String getCorpo() {
        return corpo;
    }
}
