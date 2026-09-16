package com.threeamigos.foresta.eventi;

/**
 * Richiede alla UI di reinizializzare il tutto per l'inizio del gioco
 *
 * @author Stefano Reksten
 */
public class EventoRichiestaReinizializzazioneUI extends EventoBase {

    public EventoRichiestaReinizializzazioneUI() {
        super(TipoEvento.REINIZIALIZZAZIONE);
    }

}
