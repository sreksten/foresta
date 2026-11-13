package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.tipi.TipoEvento;
import java.util.UUID;

/**
 * La classe da cui derivano tutti gli eventi del bus (vedi {@link BusEventi}). Un evento è un messaggio: porta i dati di
 * quel che è successo e non fa niente da sé. Ogni sottoclasse dichiara il suo {@link TipoEvento}, che serve solo a
 * scrivere gli eventi nel registro: il bus li smista per classe, non per tipo.
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
     * L'identificativo dell'evento, generato alla prima richiesta. È synchronized perché due thread che lo chiedono
     * insieme devono ricevere lo stesso identificativo, non due diversi.
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
