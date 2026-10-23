package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.Comando;

import java.util.Collection;

/**
 * Il giocatore chiede di vedere il bestiario, dall'inventario.
 */
public class RichiestaAperturaBestiario extends RichiestaConComandi {

    public RichiestaAperturaBestiario(Collection<Comando> possibilita) {
        super(TipoEvento.RICHIESTA_APERTURA_BESTIARIO, possibilita);
    }

}
