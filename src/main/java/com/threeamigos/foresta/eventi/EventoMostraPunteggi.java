package com.threeamigos.foresta.eventi;

/**
 * Mostra i migliori 10 giocatori di tutti i tempi
 *
 * @author Stefano Reksten
 */
public class EventoMostraPunteggi extends EventoBase {

    public EventoMostraPunteggi() {
        super(TipoEvento.MOSTRA_PUNTEGGI);
    }
}
