package com.threeamigos.foresta.strumenti;

/**
 * Un punteggio della classifica: chi l'ha fatto e quanto.
 *
 * @author Stefano Reksten
 */
public interface Punteggio {

    /**
     * Il nome di chi ha fatto il punteggio, come lo ha scritto a fine partita.
     */
    String getNome();

    /**
     * I punti.
     */
    int getPunteggio();

    /**
     * La partita che ha fatto il punteggio, o null per quelli della classifica predefinita. Non si mostra.
     */
    String getIdPartita();

}
