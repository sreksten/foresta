package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.Stato;

/**
 *
 * @author Stefano Reksten
 */
public class EventoStatoDiGioco extends EventoBase {

    private final Stato stato;

    public EventoStatoDiGioco(Stato stato) {
        super(TipoEvento.STATO_DI_GIOCO);
        this.stato = stato;
    }

    public Stato getStato() {
        return stato;
    }
}
