package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Un messaggio che il motore di gioco invia al giocatore.
 * La differenza con {@link NotificaTestoFrase} è che questo forza l'inizio di un nuovo paragrafo di testo.
 *
 * @author Stefano Reksten
 */
public class NotificaTestoParagrafo extends EventoBase {

    private final String messaggio;

    /**
     * Un messaggio per il giocatore.
     *
     * @param messaggio il messaggio da inviare al giocatore
     */
    public NotificaTestoParagrafo(String messaggio) {
        super(TipoEvento.NOTIFICA_TESTO_PARAGRAFO);
        this.messaggio = messaggio;
    }

    /**
     * Il messaggio da mostrare.
     *
     * @return il messaggio da inviare al giocatore
     */
    public String getMessaggio() {
        return messaggio;
    }

}
