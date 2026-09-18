package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.AutomaInventario;
import com.threeamigos.foresta.motore.Comando;

import java.util.Collection;

/**
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaAperturaInventarioGruppo extends EventoBase {

    private final Collection<Comando> comandiPossibili;
    private final AutomaInventario automaInventario;

    public EventoRichiestaAperturaInventarioGruppo(Collection<Comando> comandiPossibili, AutomaInventario automaInventario) {
        super(TipoEvento.RICHIESTA_APERTURA_INVENTARIO_GRUPPO);
        this.comandiPossibili = comandiPossibili;
        this.automaInventario = automaInventario;
    }

    public Collection<Comando> getComandiPossibili() {
        return comandiPossibili;
    }

    public AutomaInventario getAutomaInventario() {
        return automaInventario;
    }
}
