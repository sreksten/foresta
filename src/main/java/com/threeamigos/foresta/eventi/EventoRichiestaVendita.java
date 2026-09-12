package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConCosto;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;
import com.threeamigos.foresta.motore.TipoSpostamento;

/**
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaVendita extends EventoRichiestaSpostamento<OggettoConCosto> {

    public EventoRichiestaVendita(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                  OggettoConCosto oggettoDaVendere) {
        super(parteAttiva, parteRemota, TipoSpostamento.VENDITA, oggettoDaVendere);
    }
}
