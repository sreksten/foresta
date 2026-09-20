package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;

/**
 * Classe base per le richieste di spostamento di un Artefatto da una sorgente verso una destinazione.
 * (Personaggio <-> GruppoGiocatore, GruppoGiocatore <-> Commerciante)
 *
 * @author Stefano Reksten
 */
public abstract class ComandoSpostamentoArtefatto<T> extends EventoBase {

    private final ScambiatoreArtefatti parteAttiva;
    private final ScambiatoreArtefatti parteRemota;
    private final T oggettoDaSpostare;

    /**
     * @param parteAttiva la parte attiva della transazione (un Personaggio o l'inventario generale del GruppoGiocatore)
     * @param parteRemota la parte remota della transazione (l'inventario generale del GruppoGiocatore o un commerciante)
     * @param oggettoDaSpostare l'oggetto di interesse della transazione
     */
    public ComandoSpostamentoArtefatto(TipoEvento tipoEvento,
                                       ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                       T oggettoDaSpostare) {
        super(tipoEvento);
        this.parteAttiva = parteAttiva;
        this.parteRemota = parteRemota;
        this.oggettoDaSpostare = oggettoDaSpostare;
    }

    public final ScambiatoreArtefatti getParteAttiva() {
        return parteAttiva;
    }

    public final ScambiatoreArtefatti getParteRemota() {
        return parteRemota;
    }

    public final T getOggettoDaSpostare() {
        return oggettoDaSpostare;
    }
}
