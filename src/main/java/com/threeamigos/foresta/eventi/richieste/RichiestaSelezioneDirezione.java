package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.Comando;

import java.util.Collection;

/**
 *
 * @author Stefano Reksten
 */
public class RichiestaSelezioneDirezione extends RichiestaConComandi {

    public RichiestaSelezioneDirezione(Collection<Comando> possibilita) {
        super(TipoEvento.RICHIESTA_SELEZIONE_DIREZIONE, possibilita);
    }
}
