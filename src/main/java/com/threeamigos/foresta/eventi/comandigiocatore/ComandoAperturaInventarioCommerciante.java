package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.AutomaAcquistiArtefatti;
import com.threeamigos.foresta.motore.Comando;

import java.util.Collection;

/**
 * Il giocatore richiede l'apertura dell'inventario di un PNG con cui si può effettuare una compravendita di artefatti.
 *
 * @author Stefano Reksten
 */
public class ComandoAperturaInventarioCommerciante extends RichiestaConComandi {

    private final AutomaAcquistiArtefatti automaAcquistiArtefatti;

    public ComandoAperturaInventarioCommerciante(Collection<Comando> possibilita, AutomaAcquistiArtefatti automaAcquistiArtefatti) {
        super(TipoEvento.COMANDO_APERTURA_INVENTARIO_COMMERCIANTE, possibilita);
        this.automaAcquistiArtefatti = automaAcquistiArtefatti;
    }

    public AutomaAcquistiArtefatti getAutomaAcquistiArtefatti() {
        return automaAcquistiArtefatti;
    }
}
