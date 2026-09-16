package com.threeamigos.foresta.eventi;

/**
 * Evento interno - il motore di gioco ha un testo a disposizione.
 *
 * @author Stefano Reksten
 */
public class EventoTestoDisponibile extends EventoBase {

    private final String testo;

    public EventoTestoDisponibile(String testo) {
        super(TipoEvento.TESTO_DISPONIBILE);
        this.testo = testo;
    }

    public String getTesto() {
        return testo;
    }
}
