package com.threeamigos.foresta.eventi;

/**
 * Un messaggio che il motore di gioco invia al giocatore.
 * La differenza con EventoParagrafo è che questo continua un paragrafo precedente.
 *
 * @author Stefano Reksten
 */
public class EventoMessaggio extends EventoBase {

    private final String messaggio;

    /**
     * @param messaggio il messaggio da inviare al giocatore
     */
    public EventoMessaggio(String messaggio) {
        super(TipoEvento.MESSAGGIO);
        this.messaggio = messaggio;
    }

    /**
     * @return il messaggio da inviare al giocatore
     */
    public String getMessaggio() {
        return messaggio;
    }
}
