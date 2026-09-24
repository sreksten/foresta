package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.AutomaAcquistiArtefatti;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.modellodati.TipoNegozio;

import java.util.Collection;

/**
 * Il giocatore richiede l'apertura dell'inventario di un PNG con cui si può effettuare una compravendita di artefatti.
 *
 * @author Stefano Reksten
 */
public class ComandoAperturaInventarioCommerciante extends RichiestaConComandi {

    private final TipoNegozio negozio;
    private final AutomaAcquistiArtefatti automaAcquistiArtefatti;

    public ComandoAperturaInventarioCommerciante(Collection<Comando> possibilita, TipoNegozio negozio,
                                                 AutomaAcquistiArtefatti automaAcquistiArtefatti) {
        super(TipoEvento.COMANDO_APERTURA_INVENTARIO_COMMERCIANTE, possibilita);
        this.negozio = negozio;
        this.automaAcquistiArtefatti = automaAcquistiArtefatti;
    }

    public TipoNegozio getNegozio() {
        return negozio;
    }

    public AutomaAcquistiArtefatti getAutomaAcquistiArtefatti() {
        return automaAcquistiArtefatti;
    }
}
