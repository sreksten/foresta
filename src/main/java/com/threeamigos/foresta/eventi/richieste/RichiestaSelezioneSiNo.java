package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.Comando;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Stefano Reksten
 */
public class RichiestaSelezioneSiNo extends RichiestaConComandi {

    private static final Collection<Comando> SI_NO = Arrays.asList(Comando.SI, Comando.NO);

    public RichiestaSelezioneSiNo() {
        super(TipoEvento.RICHIESTA_SELEZIONE_SI_O_NO, SI_NO);
    }

    protected RichiestaSelezioneSiNo(TipoEvento tipoEvento) {
        super(tipoEvento, SI_NO);
    }
}
