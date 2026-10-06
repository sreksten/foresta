package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

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

    private final String idScambio;
    private final Destinazione destinazione;
    private final String uuidArtefatto;

    public ComandoScambioArtefatto(String idScambio, Destinazione destinazione, String uuidArtefatto) {
        super(TipoEvento.COMANDO_SCAMBIO_ARTEFATTO);
        this.idScambio = idScambio;
        this.destinazione = destinazione;
        this.uuidArtefatto = uuidArtefatto;
    }

    public String getIdScambio() {
        return idScambio;
    }

    public Destinazione getDestinazione() {
        return destinazione;
    }

    public String getUuidArtefatto() {
        return uuidArtefatto;
    }
}
