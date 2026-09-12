package com.threeamigos.foresta.eventi;

/**
 *
 * @author Stefano Reksten
 */
public class EventoNotificaGlobale extends EventoBase {

    private final String etichetta;
    private final String messaggio;

    public EventoNotificaGlobale(String etichetta, String messaggio) {
        super(TipoEvento.NOTIFICA_GLOBALE);
        this.etichetta = etichetta;
        this.messaggio = messaggio;
    }

    public String getEtichetta() {
        return etichetta;
    }

    public String getMessaggio() {
        return messaggio;
    }

}
