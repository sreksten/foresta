package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.AutomaInventario;
import com.threeamigos.foresta.motore.Comando;

import java.util.Collection;

/**
 * Il giocatore chiede al motore di accedere all'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public class ComandoAperturaInventarioGruppo extends RichiestaConComandi {

    private final AutomaInventario automaInventario;

    public ComandoAperturaInventarioGruppo(Collection<Comando> possibilita, AutomaInventario automaInventario) {
        super(TipoEvento.COMANDO_APERTURA_INVENTARIO_GRUPPO, possibilita);
        this.automaInventario = automaInventario;
    }

    public AutomaInventario getAutomaInventario() {
        return automaInventario;
    }
}
