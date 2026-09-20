package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.AutomaAcquistiArtefatti;
import com.threeamigos.foresta.motore.Comando;

import java.util.Collection;

/**
 * Il gruppo richiede l'apertura dell'inventario di un PNG con cui si può effettuare una compravendita di artefatti.
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaAperturaInventarioCommerciante extends EventoConComandi {

    private final AutomaAcquistiArtefatti automaAcquistiArtefatti;

    public EventoRichiestaAperturaInventarioCommerciante(Collection<Comando> possibilita, AutomaAcquistiArtefatti automaAcquistiArtefatti) {
        super(TipoEvento.RICHIESTA_APERTURA_INVENTARIO_COMMERCIANTE, possibilita);
        this.automaAcquistiArtefatti = automaAcquistiArtefatti;
    }

    public AutomaAcquistiArtefatti getAutomaAcquistiArtefatti() {
        return automaAcquistiArtefatti;
    }
}
