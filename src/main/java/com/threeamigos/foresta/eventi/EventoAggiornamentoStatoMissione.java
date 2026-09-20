package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.missioni.Missione;

/**
 *
 * @author Stefano Reksten
 */
public class EventoAggiornamentoStatoMissione extends EventoBase {

    public final Missione missione;
    public final String etichetta;
    public final String descrizione;

    public EventoAggiornamentoStatoMissione(Missione missione, String etichetta, String descrizione) {
        super(TipoEvento.AGGIORNAMENTO_STATO_MISSIONE);
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
