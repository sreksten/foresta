package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Un messaggio che il motore di gioco invia al giocatore.
 * La differenza con {@link NotificaTestoFrase} è che questo forza l'inizio di un nuovo paragrafo di testo.
 *
 * @author Stefano Reksten
 */
public class NotificaTestoParagrafo extends NotificaDiTesto {

    /**
     * @param messaggio il messaggio da inviare al giocatore (vedi {@link NotificaDiTesto} per la maiuscola iniziale)
     */
    public NotificaTestoParagrafo(String messaggio) {
        super(TipoEvento.NOTIFICA_TESTO_PARAGRAFO, messaggio);
    }

}
