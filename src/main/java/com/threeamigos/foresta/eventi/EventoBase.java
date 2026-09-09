package com.threeamigos.foresta.eventi;

/**
 *
 * @author Stefano Reksten
 */
public class EventoBase {

    private final TipoEvento tipoEvento;

    protected EventoBase(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

}
