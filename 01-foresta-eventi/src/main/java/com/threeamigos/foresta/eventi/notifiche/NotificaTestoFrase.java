package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Un messaggio che il motore di gioco invia al giocatore.
 * La differenza con {@link NotificaTestoParagrafo} è che questo continua un paragrafo precedente.
 *
 * @author Stefano Reksten
 */
public class NotificaTestoFrase extends EventoBase {

    private final String messaggio;

    /**
     * Un messaggio per il giocatore; se comincia con una lettera minuscola (un nome con l'articolo, come "the city of Fleena",
     * messo a inizio frase) la maiuscola gliela dà la notifica.
     *
     * @param messaggio il messaggio da inviare al giocatore
     */
    public NotificaTestoFrase(String messaggio) {
        super(TipoEvento.NOTIFICA_TESTO_FRASE);
        this.messaggio = conInizialeMaiuscola(messaggio);
    }

    /**
     * Il messaggio con il primo carattere maiuscolo.
     */
    static String conInizialeMaiuscola(String messaggio) {
        if (messaggio == null || messaggio.isEmpty() || Character.isUpperCase(messaggio.charAt(0))) {
            return messaggio;
        }
        return Character.toUpperCase(messaggio.charAt(0)) + messaggio.substring(1);
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
