package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.motore.Comando;

import java.util.Collection;

/**
 * Il giocatore richiede l'apertura dell'inventario di un fornitore col quale si possono effettuare unicamente acquisti
 * ma non vendite.
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaAperturaInventarioFornitore extends EventoConComandi {

    public EventoRichiestaAperturaInventarioFornitore(Collection<Comando> possibilita) {
        super(TipoEvento.RICHIESTA_APERTURA_INVENTARIO_FORNITORE, possibilita);
    }
}
