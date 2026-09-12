package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.ScambiatoreArtefatti;
import com.threeamigos.foresta.motore.TipoSpostamento;

/**
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaSpostamento<O> extends EventoBase {

    private final ScambiatoreArtefatti parteAttiva;
    private final ScambiatoreArtefatti parteRemota;
    private final TipoSpostamento tipoSpostamento;
    private final O oggettoDaSpostare;

    public EventoRichiestaSpostamento(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                      TipoSpostamento tipoSpostamento, O oggettoDaSpostare) {
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

    public final O getOggettoDaSpostare() {
        return oggettoDaSpostare;
    }
}
