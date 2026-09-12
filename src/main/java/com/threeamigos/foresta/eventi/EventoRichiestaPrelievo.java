package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConPeso;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;
import com.threeamigos.foresta.motore.TipoSpostamento;

/**
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaPrelievo extends EventoRichiestaSpostamento<OggettoConPeso> {

    public EventoRichiestaPrelievo(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                   OggettoConPeso oggettoDaPrelevare) {
        super(parteAttiva, parteRemota, TipoSpostamento.SPOSTAMENTO_DA_INVENTARIO_GRUPPO_A_PERSONAGGIO, oggettoDaPrelevare);
    }
}
