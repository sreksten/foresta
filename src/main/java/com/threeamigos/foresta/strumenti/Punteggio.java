package com.threeamigos.foresta.strumenti;

/**
 *
 * @author Stefano Reksten
 */
public interface Punteggio {

    String getNome();

    int getPunteggio();

    /**
     * La partita che ha fatto il punteggio, o null per quelli della classifica predefinita. Non si mostra.
     */
    String getIdPartita();

}
