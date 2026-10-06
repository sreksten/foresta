package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.OggettoConCosto;
import com.threeamigos.foresta.interfacce.VistaScambio;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;

/**
 * Il giocatore chiede di poter comprare un Artefatto da un commerciante e metterlo nell'inventario generale.
 * L'esito viene deciso dal costo dell'Artefatto.
 *
 * @author Stefano Reksten
 */
public class InternoAcquistoArtefatto extends InternoSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param parteAttiva l'inventario generale del GruppoGiocatore
     * @param parteRemota il commerciante
     * @param oggettoDaAcquistare l'oggetto di interesse della transazione
     * @param scambio lo scambio aperto da cui viene, o null
     */
    public InternoAcquistoArtefatto(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                    OggettoConCosto oggettoDaAcquistare, VistaScambio scambio) {
        super(TipoEvento.INTERNO_ACQUISTO_ARTEFATTO, parteAttiva, parteRemota, oggettoDaAcquistare, scambio);
    }
}
