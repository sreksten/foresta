package com.threeamigos.foresta.motore;

/**
 * Simula un lancio di dado.
 *
 * @author Stefano Reksten
 */
public class Dado {

    /**
     * Simula un lancio di dado a N facce.<p>
     * @param N il numero di facce del dado (maggiore di uno)
     * @return il risultato del lancio
     */
    public static int tira(int N) {
        if (N <= 1) {
            throw new IllegalArgumentException("Il numero di facce deve essere maggiore di 1");
        }
        return tira(1, N);
    }

    /**
     * Produce un numero casuale tra min e max compresi (min deve essere strettamente minore di max)
     * @param min il valore minimo
     * @param max il valore massimo
     * @return il risultato del lancio
     */
    public static int tira(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("Il valore minimo deve essere minore al valore massimo");
        }
        return (int) (Math.random() * (max - min + 1)) + min;
    }
}
