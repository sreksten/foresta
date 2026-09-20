package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.Comando;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Stefano Reksten
 */
public class EventoSelezioneSiNo extends EventoConComandi {

    private static final Collection<Comando> SI_NO = Arrays.asList(Comando.SI, Comando.NO);

    public EventoSelezioneSiNo() {
        super(TipoEvento.RICHIESTA_SELEZIONE_SI_O_NO, SI_NO);
    }

    protected EventoSelezioneSiNo(TipoEvento tipoEvento) {
        super(tipoEvento, SI_NO);
    }
}
