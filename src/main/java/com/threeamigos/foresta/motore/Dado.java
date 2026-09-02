package com.threeamigos.foresta.motore;

import java.util.List;

/**
 * Simula un lancio di dado.
 *
 * @author Stefano Reksten
 */
public class Dado {


    /**
     * Simula un lancio di dado a N facce con N >= 1.
     * <p> Questa funzione è utile quando occorre passare la dimensione di un array o di una lista per recuperare
     * un elemento casuale.
     * Normalmente un dado non ha una faccia sola, ma in caso non si sappia di preciso quale sarà il numero di facce
     * perché per esempio si sta filtrando su una lista, si può usare questo metodo.
     * L'unico controllo da fare è che la lista non sia vuota (basta che N sia > 0)
     * @param N il numero di facce del dado
     * @return il risultato del lancio, da 1 a N inclusi
     */
    public static int tiraAncheAUnaFaccia(int N) {
        if (N < 1) {
            Logger.log("**************************ERRORE FATALE! Dado tira(" + N + ")");
            throw new IllegalArgumentException("Il numero di facce deve essere maggiore di zero");
        }
        if (N == 1) {
            return 1;
        }
        return tira(1, N);
    }

    /**
     * Simula un lancio di dado a (max - min + 1) facce con numero minimo >= 1.
     * <p>L'unico controllo è che min sia <= max
     * @param min il valore minimo
     * @param max il valore massimo
     * @return il risultato del lancio, da 1 a N inclusi
     */
    public static int tiraAncheSenzaRange(int min, int max) {
        if (min > max) {
            Logger.log("**************************ERRORE FATALE! Dado tira(" + min + ", " + max + ")");
            throw new IllegalArgumentException("Il valore minimo deve essere minore o uguale al valore massimo");
        }
        if (min == max) {
            return min;
        }
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    /**
     * Simula un lancio di dado a N facce.<p>
     * @param N il numero di facce del dado (maggiore di uno)
     * @return il risultato del lancio, da 1 a N inclusi
     */
    public static int tira(int N) {
        if (N <= 1) {
            Logger.log("**************************ERRORE FATALE! Dado tira(" + N + ")");
            throw new IllegalArgumentException("Il numero di facce deve essere maggiore di 1");
        }
        return tira(1, N);
    }

    /**
     * Produce un numero casuale tra min e max compresi (min deve essere strettamente minore di max)
     * @param min il valore minimo
     * @param max il valore massimo
     * @return il risultato del lancio, da min a max inclusi
     */
    public static int tira(int min, int max) {
        if (min >= max) {
            Logger.log("**************************ERRORE FATALE! Dado tira(" + min + ", " + max + ")");
            throw new IllegalArgumentException("Il valore minimo deve essere minore al valore massimo");
        }
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    /**
     * <b>Sfila un elemento casuale</b> da una collezione e lo restituisce
     */
    public static <T> T selezionaCasualmente(List<T> elencoIniziale) {
        T t = null;
        int size = elencoIniziale.size();
        if (size > 0) {
            if (size == 1) {
                t = elencoIniziale.get(0);
                elencoIniziale.remove(0);
            } else {
                int indice = Dado.tira(size) - 1;
                t = elencoIniziale.get(indice);
                elencoIniziale.remove(indice);
            }
        }
        return t;
    }
}
