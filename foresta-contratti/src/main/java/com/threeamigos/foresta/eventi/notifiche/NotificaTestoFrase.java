package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Un messaggio che il motore di gioco invia al giocatore.
 * La differenza con {@link NotificaTestoParagrafo} è che questo continua un paragrafo precedente.
 *
 * @author Stefano Reksten
 */
public class NotificaTestoFrase extends EventoBase {

    private final String messaggio;

    /**
     * @param messaggio il messaggio da inviare al giocatore
     */
    public NotificaTestoFrase(String messaggio) {
        super(TipoEvento.NOTIFICA_TESTO_FRASE);
        this.messaggio = messaggio;
    }

    /**
     * @return il messaggio da inviare al giocatore
     */
    public String getMessaggio() {
        return messaggio;
    }
}
