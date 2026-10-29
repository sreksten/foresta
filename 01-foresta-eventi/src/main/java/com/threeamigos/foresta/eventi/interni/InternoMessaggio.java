package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoBase;
import com.threeamigos.foresta.tipi.TipoEvento;

/**
 * Un messaggio interno del sistema destinato a un sistema di logging.
 *
 * @author Stefano Reksten
 */
public class InternoMessaggio extends EventoBase {

    private final String messaggioInterno;

    /**
     * Un evento con quel messaggio.
     */
    public InternoMessaggio(String messaggioInterno) {
        super(TipoEvento.INTERNO_MESSAGGIO);
        this.messaggioInterno = messaggioInterno;
    }

    /**
     * Il messaggio d'errore.
     */
    public String getMessaggioInterno() {
        return messaggioInterno;
    }

}
