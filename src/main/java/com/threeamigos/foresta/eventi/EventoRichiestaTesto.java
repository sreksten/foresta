package com.threeamigos.foresta.eventi;

/**
 * Il gioco richiede un testo al giocatore (ad esempio il nome del personaggio).
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaTesto extends EventoBase {

    private final String richiesta;

    public EventoRichiestaTesto(String richiesta) {
        super(TipoEvento.RICHIESTA_TESTO);
        this.richiesta = richiesta;
    }

    public String getRichiesta() {
        return richiesta;
    }
}
