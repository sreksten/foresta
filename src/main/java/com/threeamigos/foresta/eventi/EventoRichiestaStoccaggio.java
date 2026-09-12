package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConPeso;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;
import com.threeamigos.foresta.motore.TipoSpostamento;

/**
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaStoccaggio extends EventoRichiestaSpostamento<OggettoConPeso> {

    public EventoRichiestaStoccaggio(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                     OggettoConPeso oggettoDaStoccare) {
        super(parteAttiva, parteRemota, TipoSpostamento.SPOSTAMENTO_DA_PERSONAGGIO_A_INVENTARIO_GRUPPO, oggettoDaStoccare);
    }
}
