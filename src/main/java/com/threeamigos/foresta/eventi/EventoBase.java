package com.threeamigos.foresta.eventi;

import java.util.UUID;

/**
 *
 * @author Stefano Reksten
 */
public class EventoBase {

    private final String uuid = UUID.randomUUID().toString();
    private final TipoEvento tipoEvento;

    protected EventoBase(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getUuid() {
        return uuid;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

}
