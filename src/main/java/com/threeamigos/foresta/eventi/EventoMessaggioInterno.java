package com.threeamigos.foresta.eventi;

/**
 * Un messaggio interno del sistema destinato a un sistema di logging.
 *
 * @author Stefano Reksten
 */
public class EventoMessaggioInterno extends EventoBase {

    private final String messaggioInterno;

    public EventoMessaggioInterno(String messaggioInterno) {
        super(TipoEvento.MESSAGGIO_INTERNO);
        this.messaggioInterno = messaggioInterno;
    }

    public String getMessaggioInterno() {
        return messaggioInterno;
    }
}
