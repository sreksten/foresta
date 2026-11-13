package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Un messaggio che il motore di gioco invia al giocatore.
 * La differenza con {@link NotificaTestoParagrafo} è che questo continua un paragrafo precedente.
 *
 * @author Stefano Reksten
 */
public class NotificaTestoFrase extends NotificaDiTesto {

    /**
     * @param messaggio il messaggio da inviare al giocatore (vedi {@link NotificaDiTesto} per la maiuscola iniziale)
     */
    public NotificaTestoFrase(String messaggio) {
        super(TipoEvento.NOTIFICA_TESTO_FRASE, messaggio);
    }

}
