package com.threeamigos.foresta.eventi.richieste;

import com.threeamigos.foresta.eventi.RichiestaConComandi;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.interfacce.VistaOffertaConsumabile;
import com.threeamigos.foresta.tipi.Comando;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Il giocatore richiede l'apertura dell'inventario di un fornitore col quale si possono effettuare unicamente acquisti
 * ma non vendite. Porta il listino, che il motore costruisce a ogni apertura (l'oroscopo dell'alchimista sta nel suo
 * intermezzo d'ingresso, IntermezzoOroscopo).
 *
 * @author Stefano Reksten
 */
public class RichiestaAperturaInventarioFornitore extends RichiestaConComandi {

    private final List<VistaOffertaConsumabile> offerte;

    public RichiestaAperturaInventarioFornitore(Collection<Comando> possibilita,
                                              List<? extends VistaOffertaConsumabile> offerte) {
        super(TipoEvento.RICHIESTA_APERTURA_INVENTARIO_FORNITORE, possibilita);
        this.offerte = Collections.unmodifiableList(offerte);
    }

    public List<VistaOffertaConsumabile> getOfferte() {
        return offerte;
    }

}
