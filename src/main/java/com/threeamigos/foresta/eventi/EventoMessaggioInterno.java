package com.threeamigos.foresta.eventi;

/**
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
