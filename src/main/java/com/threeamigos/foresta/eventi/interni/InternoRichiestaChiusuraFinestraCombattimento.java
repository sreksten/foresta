package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 *
 * @author Stefano Reksten
 */
public class InternoRichiestaChiusuraFinestraCombattimento extends EventoBase {

    public InternoRichiestaChiusuraFinestraCombattimento() {
        super(TipoEvento.INTERNO_STATO_FINESTRA_COMBATTIMENTO);
    }

}
