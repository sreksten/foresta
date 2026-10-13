package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaPersonaggio;
import com.threeamigos.foresta.interfacce.VistaScambio;
import com.threeamigos.foresta.tipi.Comando;

import java.util.Collection;

/**
 * Il giocatore chiede al motore di accedere all'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class RichiestaAperturaInventarioGruppo extends RichiestaConComandi {

    private final VistaScambio scambio;
    private final VistaPersonaggio personaggio;

    /**
     * @param scambio fra l'inventario del personaggio (parte attiva) e quello del gruppo (parte remota)
     * @param personaggio il personaggio di cui si mostra l'inventario
     */
    public RichiestaAperturaInventarioGruppo(Collection<Comando> possibilita, VistaScambio scambio, VistaPersonaggio personaggio) {
        super(TipoEvento.RICHIESTA_APERTURA_INVENTARIO_GRUPPO, possibilita);
        this.scambio = scambio;
        this.personaggio = personaggio;
    }

    public VistaScambio getScambio() {
        return scambio;
    }

    public VistaPersonaggio getPersonaggio() {
        return personaggio;
    }
}
