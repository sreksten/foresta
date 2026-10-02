package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * In una locanda, nella foresta o in città, è stato servito un pasto: a tutto il gruppo
 * o, se le monete non bastano, a uno solo dei suoi personaggi.
 */
public class InternoPastoConsumatoInLocanda extends EventoBase {

    public InternoPastoConsumatoInLocanda() {
        super(TipoEvento.INTERNO_PASTO_CONSUMATO_IN_LOCANDA);
    }
}
