package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.AutomaInventario;
import com.threeamigos.foresta.motore.Comando;

import java.util.Collection;

/**
 * Il giocatore chiede al motore di accedere all'inventario del gruppo
 * @author Stefano Reksten
 */
public class EventoRichiestaAperturaInventarioGruppo extends EventoConComandi {

    private final AutomaInventario automaInventario;

    public EventoRichiestaAperturaInventarioGruppo(Collection<Comando> possibilita, AutomaInventario automaInventario) {
        super(TipoEvento.RICHIESTA_APERTURA_INVENTARIO_GRUPPO, possibilita);
        this.automaInventario = automaInventario;
    }

    public AutomaInventario getAutomaInventario() {
        return automaInventario;
    }
}
