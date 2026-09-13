package com.threeamigos.foresta.eventi;

/**
 * Un messaggio che il motore di gioco invia al giocatore.
 * La differenza con EventoMessaggio è che questo forza l'inizio di un nuovo paragrafo di testo.
 *
 * @author Stefano Reksten
 */
public class EventoParagrafo extends EventoBase {

    private final String messaggio;

    /**
     * @param messaggio il messaggio da inviare al giocatore
     */
    public EventoParagrafo(String messaggio) {
        super(TipoEvento.PARAGRAFO);
        this.messaggio = messaggio;
    }

    /**
     * @return il messaggio da inviare al giocatore
     */
    public String getMessaggio() {
        return messaggio;
    }
}
