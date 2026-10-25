package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * La richiesta di chiudere la finestra del combattimento.
 *
 * @author Stefano Reksten
 */
public class InternoRichiestaChiusuraFinestraCombattimento extends EventoBase {

    public InternoRichiestaChiusuraFinestraCombattimento() {
        super(TipoEvento.INTERNO_RICHIESTA_CHIUSURA_FINESTRA_COMBATTIMENTO);
    }

}
