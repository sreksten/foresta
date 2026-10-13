package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.Comando;

import java.util.Collection;

/**
 * Il giocatore chiede di vedere la pagina dei trofei, dall'inventario.
 */
public class RichiestaAperturaTrofei extends RichiestaConComandi {

    public RichiestaAperturaTrofei(Collection<Comando> possibilita) {
        super(TipoEvento.RICHIESTA_APERTURA_TROFEI, possibilita);
    }
}
