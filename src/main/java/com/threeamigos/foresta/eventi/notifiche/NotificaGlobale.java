package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Un evento di notifica globale (un messaggio più importante degli altri) da mandare al giocatore.
 * Ad esempio, la notifica che una nuova missione è iniziata.
 *
 * @author Stefano Reksten
 */
public class NotificaGlobale extends EventoBase {

    private final String etichetta;
    private final String messaggio;

    /**
     * @param etichetta una etichetta da mostrare al giocatore (es: "Nuova missione!")
     * @param messaggio il messaggio da inviare al giocatore (es: "Fai questa cosa")
     */
    public NotificaGlobale(String etichetta, String messaggio) {
        super(TipoEvento.NOTIFICA_GLOBALE);
        this.etichetta = etichetta;
        this.messaggio = messaggio;
    }

    /**
     * @return l'etichetta da mostrare al giocatore
     */
    public String getEtichetta() {
        return etichetta;
    }

    /**
     * @return il messaggio da inviare al giocatore
     */
    public String getMessaggio() {
        return messaggio;
    }
}
