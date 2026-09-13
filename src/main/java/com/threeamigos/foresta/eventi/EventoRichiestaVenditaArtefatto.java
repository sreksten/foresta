package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.OggettoConCosto;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;
import com.threeamigos.foresta.motore.TipoSpostamento;

/**
 * Il giocatore chiede di poter vendere un Artefatto presente nell'inventario generale a un commerciante.
 * L'esito viene deciso dal costo dell'oggetto.
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaVenditaArtefatto extends EventoRichiestaSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param parteAttiva l'inventario generale del GruppoGiocatore
     * @param parteRemota il commerciante
     * @param oggettoDaVendere l'oggetto di interesse della transazione
     */
    public EventoRichiestaVenditaArtefatto(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                           OggettoConCosto oggettoDaVendere) {
        super(parteAttiva, parteRemota, TipoSpostamento.VENDITA, oggettoDaVendere);
    }
}
