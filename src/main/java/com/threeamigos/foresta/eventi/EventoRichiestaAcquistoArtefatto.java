package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConCosto;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;
import com.threeamigos.foresta.motore.TipoSpostamento;

/**
 * Il giocatore chiede di poter comprare un Artefatto da un commerciante e metterlo nell'inventario generale.
 * L'esito viene deciso dal costo dell'oggetto.
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaAcquistoArtefatto extends EventoRichiestaSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param parteAttiva l'inventario generale del GruppoGiocatore
     * @param parteRemota il commerciante
     * @param oggettoDaAcquistare l'oggetto di interesse della transazione
     */
    public EventoRichiestaAcquistoArtefatto(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                            OggettoConCosto oggettoDaAcquistare) {
        super(parteAttiva, parteRemota, TipoSpostamento.ACQUISTO, oggettoDaAcquistare);
    }
}
