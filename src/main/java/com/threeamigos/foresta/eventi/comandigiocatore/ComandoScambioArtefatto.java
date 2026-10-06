package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaArtefatto;
import com.threeamigos.foresta.interfacce.VistaScambio;

/**
 * Il giocatore sposta un artefatto da una parte all'altra di uno scambio aperto (doppio click nella schermata).
 * Lo scambio decide che cosa vuol dire: prelievo o stoccaggio nell'inventario, acquisto o vendita in un negozio,
 * banco di lavoro dall'incantatore (vedi AutomaScambiatoreArtefatti).
 */
public class ComandoScambioArtefatto extends EventoBase {

    public enum Destinazione {
        PARTE_ATTIVA,
        PARTE_REMOTA
    }

    private final VistaScambio scambio;
    private final Destinazione destinazione;
    private final VistaArtefatto artefatto;

    public ComandoScambioArtefatto(VistaScambio scambio, Destinazione destinazione, VistaArtefatto artefatto) {
        super(TipoEvento.COMANDO_SCAMBIO_ARTEFATTO);
        this.scambio = scambio;
        this.destinazione = destinazione;
        this.artefatto = artefatto;
    }

    public VistaScambio getScambio() {
        return scambio;
    }

    public Destinazione getDestinazione() {
        return destinazione;
    }

    public VistaArtefatto getArtefatto() {
        return artefatto;
    }
}
