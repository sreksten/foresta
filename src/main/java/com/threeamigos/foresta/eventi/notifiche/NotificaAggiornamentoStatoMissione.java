package com.threeamigos.foresta.eventi.notifiche;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.missioni.Missione;

/**
 * Il motore informa il giocatore riguardo allo stato generale di una Missione.
 *
 * @author Stefano Reksten
 */
public class NotificaAggiornamentoStatoMissione extends EventoBase {

    public final Missione missione;
    public final String etichetta;
    public final String descrizione;

    public NotificaAggiornamentoStatoMissione(Missione missione, String etichetta, String descrizione) {
        super(TipoEvento.NOTIFICA_AGGIORNAMENTO_STATO_MISSIONE);
        this.missione = missione;
        this.etichetta = etichetta;
        this.descrizione = descrizione;
    }

    public Missione getMissione() {
        return missione;
    }

    public String getEtichetta() {
        return etichetta;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
