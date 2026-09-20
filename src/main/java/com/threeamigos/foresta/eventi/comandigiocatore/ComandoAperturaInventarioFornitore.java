package com.threeamigos.foresta.eventi.comandigiocatore;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.motore.Comando;

import java.util.Collection;

/**
 * Il giocatore richiede l'apertura dell'inventario di un fornitore col quale si possono effettuare unicamente acquisti
 * ma non vendite.
 *
 * @author Stefano Reksten
 */
public class ComandoAperturaInventarioFornitore extends RichiestaConComandi {

    public ComandoAperturaInventarioFornitore(Collection<Comando> possibilita) {
        super(TipoEvento.COMANDO_APERTURA_INVENTARIO_FORNITORE, possibilita);
    }
}
