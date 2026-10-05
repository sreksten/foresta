package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.tipi.Comando;

import java.util.Collection;

/**
 * Il giocatore chiede di vedere la pagina dei trofei, dall'inventario.
 */
public class ComandoAperturaTrofei extends RichiestaConComandi {

    public ComandoAperturaTrofei(Collection<Comando> possibilita) {
        super(TipoEvento.COMANDO_APERTURA_TROFEI, possibilita);
    }
}
