package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.AutomaInventario;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.Collection;

/**
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaInventario extends EventoBase {

    private final Collection<Comando> comandiPossibili;
    private final AutomaInventario automaInventario;
    private final Personaggio preselezionato;

    public EventoRichiestaInventario(Collection<Comando> comandiPossibili, AutomaInventario automaInventario,
                                     Personaggio preselezionato) {
        super(TipoEvento.RICHIESTA_INVENTARIO);
        this.comandiPossibili = comandiPossibili;
        this.automaInventario = automaInventario;
        this.preselezionato = preselezionato;
    }

    public Collection<Comando> getComandiPossibili() {
        return comandiPossibili;
    }

    public AutomaInventario getAutomaInventario() {
        return automaInventario;
    }

    public Personaggio getPreselezionato() {
        return preselezionato;
    }
}
