package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Il livello di un Personaggio è stato aumentato.
 *
 * @author Stefano Reksten
 */
public class NotificaAumentoLivelloPersonaggio extends EventoSuPersonaggio {

    private final int livelloPrecedente;
    private final int livelloAttuale;

    public NotificaAumentoLivelloPersonaggio(Personaggio personaggio, int livelloPrecedente, int livelloAttuale) {
        super(TipoEvento.NOTIFICA_AUMENTO_LIVELLO_PERSONAGGIO, personaggio);
        this.livelloPrecedente = livelloPrecedente;
        this.livelloAttuale = livelloAttuale;
    }

    public int getLivelloPrecedente() {
        return livelloPrecedente;
    }

    public int getLivelloAttuale() {
        return livelloAttuale;
    }
}
