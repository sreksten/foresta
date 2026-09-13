package com.threeamigos.foresta.eventi;

/**
 * La differenza con EventoMessaggio è che questo forza l'inizio di un nuovo paragrafo di testo.
 * @author Stefano Reksten
 */
public class EventoParagrafo extends EventoBase {

    private final String messaggio;

    public EventoParagrafo(String messaggio) {
        super(TipoEvento.PARAGRAFO);
        this.messaggio = messaggio;
    }

    public String getMessaggio() {
        return messaggio;
    }
}
