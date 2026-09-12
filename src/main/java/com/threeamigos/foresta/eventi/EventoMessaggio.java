package com.threeamigos.foresta.eventi;

/**
 * La differenza con EventoParagrafo è che questo continua un paragrafo precedente.
 * @author Stefano Reksten
 */
public class EventoMessaggio extends EventoBase {

    private final String messaggio;

    public EventoMessaggio(String messaggio) {
        super(TipoEvento.MESSAGGIO);
        this.messaggio = messaggio;
    }

    public String getMessaggio() {
        return messaggio;
    }
}
