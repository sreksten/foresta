package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.tipi.TipoEvento;
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

    /**
     * Un evento del tipo dato.
     */
    protected EventoBase(TipoEvento tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    /**
     * L'identificativo dell'evento, generato alla prima richiesta.
     */
    public synchronized String getUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    /**
     * Il tipo dell'evento.
     */
    public TipoEvento getTipoEvento() {
        return tipoEvento;
    }

}
