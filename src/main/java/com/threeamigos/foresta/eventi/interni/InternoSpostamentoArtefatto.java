package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaScambio;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;

/**
 * Classe base per le richieste di spostamento di un Artefatto da una sorgente verso una destinazione.
 * (Personaggio <-> GruppoGiocatore, GruppoGiocatore <-> Commerciante)
 *
 * @author Stefano Reksten
 */
public abstract class InternoSpostamentoArtefatto<T> extends EventoBase {

    private final ScambiatoreArtefatti parteAttiva;
    private final ScambiatoreArtefatti parteRemota;
    private final T oggettoDaSpostare;
    private final VistaScambio scambio;

    /**
     * @param parteAttiva la parte attiva della transazione (un Personaggio o l'inventario generale del GruppoGiocatore)
     * @param parteRemota la parte remota della transazione (l'inventario generale del GruppoGiocatore o un commerciante)
     * @param oggettoDaSpostare l'oggetto di interesse della transazione
     * @param scambio lo scambio aperto da cui viene il comando, o null se non viene da una schermata di scambio: le
     *                schermate rispondono solo alle notifiche del proprio
     */
    public InternoSpostamentoArtefatto(TipoEvento tipoEvento,
                                       ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                       T oggettoDaSpostare, VistaScambio scambio) {
        super(tipoEvento);
        this.parteAttiva = parteAttiva;
        this.parteRemota = parteRemota;
        this.oggettoDaSpostare = oggettoDaSpostare;
        this.scambio = scambio;
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

    public final VistaScambio getScambio() {
        return scambio;
    }
}
