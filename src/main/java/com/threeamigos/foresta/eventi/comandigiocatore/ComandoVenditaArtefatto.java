package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.OggettoConCosto;
import com.threeamigos.foresta.motore.ScambiatoreArtefatti;

/**
 * Il giocatore chiede di poter vendere un Artefatto presente nell'inventario del gruppo a un commerciante.
 * L'esito viene deciso dal costo dell'Artefatto.
 * <p>
 * (NOTA: in questo momento i commercianti non hanno limite di budget, quindi la vendita viene approvata automaticamente.)
 *
 * @author Stefano Reksten
 */
public class ComandoVenditaArtefatto extends ComandoSpostamentoArtefatto<OggettoConCosto> {

    /**
     * @param parteAttiva l'inventario generale del GruppoGiocatore
     * @param parteRemota il commerciante
     * @param oggettoDaVendere l'oggetto di interesse della transazione
     */
    public ComandoVenditaArtefatto(ScambiatoreArtefatti parteAttiva, ScambiatoreArtefatti parteRemota,
                                   OggettoConCosto oggettoDaVendere) {
        super(TipoEvento.COMANDO_VENDITA_ARTEFATTO, parteAttiva, parteRemota, oggettoDaVendere);
    }
}
