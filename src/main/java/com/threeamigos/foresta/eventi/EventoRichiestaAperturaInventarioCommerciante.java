package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.AutomaAcquistiArtefatti;
import com.threeamigos.foresta.motore.Comando;

import java.util.Collection;

/**
 * Il gruppo richiede l'apertura dell'inventario di un PNG con cui si può effettuare una compravendita di artefatti.
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaAperturaInventarioCommerciante extends EventoBase {

    private final Collection<Comando> comandiPossibili;
    private final AutomaAcquistiArtefatti automaAcquistiArtefatti;

    public EventoRichiestaAperturaInventarioCommerciante(Collection<Comando> comandiPossibili, AutomaAcquistiArtefatti automaAcquistiArtefatti) {
        super(TipoEvento.RICHIESTA_APERTURA_INVENTARIO_COMMERCIANTE);
        this.comandiPossibili = comandiPossibili;
        this.automaAcquistiArtefatti = automaAcquistiArtefatti;
    }

    public Collection<Comando> getComandiPossibili() {
        return comandiPossibili;
    }

    public AutomaAcquistiArtefatti getAutomaAcquistiArtefatti() {
        return automaAcquistiArtefatti;
    }
}
