package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.OggettoConCosto;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;

/**
 * Il giocatore chiede di poter comprare un Artefatto da un commerciante e metterlo nell'inventario generale.
 * L'esito viene deciso dal costo dell'Artefatto.
 *
 * @author Stefano Reksten
 */
public class ComandoAcquistoArtefatto extends ComandoSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param parteAttiva l'inventario generale del GruppoGiocatore
     * @param parteRemota il commerciante
     * @param oggettoDaAcquistare l'oggetto di interesse della transazione
     */
    public ComandoAcquistoArtefatto(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                    OggettoConCosto oggettoDaAcquistare) {
        super(TipoEvento.COMANDO_ACQUISTO_ARTEFATTO, parteAttiva, parteRemota, oggettoDaAcquistare);
    }
}
