package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.ScambiatoreArtefatti;
import com.threeamigos.foresta.motore.TipoSpostamento;

/**
 * Classe base per le richieste di spostamento di un Artefatto da una sorgente verso una destinazione.
 * (Personaggio -> GruppoGiocatore, GruppoGiocatore -> Commerciante, GruppoGiocatore -> Personaggio...)
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaSpostamentoArtefatto<T> extends EventoBase {

    private final ScambiatoreArtefatti parteAttiva;
    private final ScambiatoreArtefatti parteRemota;
    private final TipoSpostamento tipoSpostamento;
    private final T oggettoDaSpostare;

    /**
     * @param parteAttiva la parte attiva della transazione (un Personaggio o l'inventario generale del GruppoGiocatore)
     * @param parteRemota la parte remota della transazione (l'inventario generale del GruppoGiocatore o un commerciante)
     * @param tipoSpostamento il tipo di spostamento richiesto
     * @param oggettoDaSpostare l'oggetto di interesse della transazione
     */
    public EventoRichiestaSpostamentoArtefatto(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                               TipoSpostamento tipoSpostamento, T oggettoDaSpostare) {
        super(TipoEvento.RICHIESTA_SPOSTAMENTO_OGGETTO);
        this.parteAttiva = parteAttiva;
        this.parteRemota = parteRemota;
        this.tipoSpostamento = tipoSpostamento;
        this.oggettoDaSpostare = oggettoDaSpostare;
    }

    public final ScambiatoreArtefatti getParteAttiva() {
        return parteAttiva;
    }

    public final ScambiatoreArtefatti getParteRemota() {
        return parteRemota;
    }

    public final TipoSpostamento getTipoSpostamento() {
        return tipoSpostamento;
    }

    public final T getOggettoDaSpostare() {
        return oggettoDaSpostare;
    }
}
