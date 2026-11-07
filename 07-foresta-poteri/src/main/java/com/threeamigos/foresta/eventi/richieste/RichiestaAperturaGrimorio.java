package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.tipi.TipoEvento;
import com.threeamigos.foresta.tipi.Comando;

import java.util.Collection;

/**
 * Il giocatore chiede di vedere il grimorio, dall'inventario.
 */
public class RichiestaAperturaGrimorio extends RichiestaConComandi {

    public RichiestaAperturaGrimorio(Collection<Comando> possibilita) {
        super(TipoEvento.RICHIESTA_APERTURA_GRIMORIO, possibilita);
    }

}
