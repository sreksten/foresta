package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConCosto;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;
import com.threeamigos.foresta.motore.TipoSpostamento;

/**
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaAcquisto extends EventoRichiestaSpostamento<OggettoConCosto> {

    public EventoRichiestaAcquisto(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                   OggettoConCosto oggettoDaAcquistare) {
        super(parteAttiva, parteRemota, TipoSpostamento.ACQUISTO, oggettoDaAcquistare);
    }
}
