package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.eventi.TipoEvento;

/**
 * Un messaggio interno del sistema destinato a un sistema di logging.
 *
 * @author Stefano Reksten
 */
public class InternoMessaggio extends EventoBase {

    private final String messaggioInterno;

    public InternoMessaggio(String messaggioInterno) {
        super(TipoEvento.INTERNO_MESSAGGIO);
        this.messaggioInterno = messaggioInterno;
    }

    public String getMessaggioInterno() {
        return messaggioInterno;
    }
}
