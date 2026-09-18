package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.Comando;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaConfermaUscita extends EventoBase {

    private final Collection<Comando> comandiPossibili;

    public EventoRichiestaConfermaUscita(Comando ... comandiPossibili) {
        super(TipoEvento.RICHIESTA_CONFERMA_USCITA);
        this.comandiPossibili = Arrays.asList(comandiPossibili);
    }

    public Collection<Comando> getComandiPossibili() {
        return comandiPossibili;
    }
}
