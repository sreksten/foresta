package com.threeamigos.foresta.eventi;

import java.util.UUID;

/**
 * Classe base astratta per eventi che accadono all'interno del motore.
 *
 * @author Stefano Reksten
 */
public class EventoBase {

    // Si genera alla prima richiesta: quasi nessuno lo chiede, e gli eventi sono centinaia a ogni locazione
    private String uuid;
    private final TipoEvento tipoEvento;

    protected EventoBase(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public synchronized String getUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

}
