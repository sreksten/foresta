package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaScambio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoNegozio;

import java.util.Collection;

/**
 * Il giocatore richiede l'apertura dell'inventario di un PNG con cui si può effettuare una compravendita di artefatti.
 *
 * @author Stefano Reksten
 */
public class RichiestaAperturaInventarioCommerciante extends RichiestaConComandi {

    private final TipoNegozio negozio;
    private final VistaScambio scambio;

    /**
     * @param scambio fra l'inventario del gruppo (parte attiva) e il magazzino del negozio (parte remota)
     */
    public RichiestaAperturaInventarioCommerciante(Collection<Comando> possibilita, TipoNegozio negozio,
                                                 VistaScambio scambio) {
        super(TipoEvento.RICHIESTA_APERTURA_INVENTARIO_COMMERCIANTE, possibilita);
        this.negozio = negozio;
        this.scambio = scambio;
    }

    public TipoNegozio getNegozio() {
        return negozio;
    }

    public VistaScambio getScambio() {
        return scambio;
    }
}
