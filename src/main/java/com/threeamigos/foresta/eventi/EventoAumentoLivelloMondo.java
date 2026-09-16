package com.threeamigos.foresta.eventi;

/**
 *
 * @author Stefano Reksten
 */
public class EventoAumentoLivelloMondo extends EventoBase {

    private final int livello;

    public EventoAumentoLivelloMondo(int livello) {
        super(TipoEvento.MONDO_AUMENTO_LIVELLO);
        this.livello = livello;
    }

    public int getLivello() {
        return livello;
    }
}
