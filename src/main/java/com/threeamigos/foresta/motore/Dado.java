package com.threeamigos.foresta.motore;

import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Simula un lancio di dado. Tutto il caso delle decisioni di gioco passa da qui, così nei test si può:
 * <ul>
 *     <li>fissare il seme ({@link #impostaSeme}) per rendere ripetibile una partita intera;</li>
 *     <li>truccare i prossimi lanci ({@link #trucca}) per provare un caso preciso.</li>
 * </ul>
 * Il generatore di artefatti usa direttamente {@link #sorgente()}: segue il seme ma non consuma i lanci truccati,
 * che restano per le decisioni. Le grammatiche (GrammarBean) hanno un loro Random e non seguono il seme.
 *
 * @author Stefano Reksten
 */
public class Dado {

    private static final Random SORGENTE = new Random();

    // I prossimi risultati truccati, in ordine: Integer per i lanci, Double per le probabilità
    private static final Deque<Number> TRUCCHI = new ConcurrentLinkedDeque<>();

    /**
     * Il generatore da cui viene tutto il caso: chi lo tiene segue anche un seme impostato dopo.
     */
    public static Random sorgente() {
        return SORGENTE;
    }

    /**
     * Fissa il seme: da qui in poi i lanci (non truccati) sono ripetibili.
     */
    public static void impostaSeme(long seme) {
        SORGENTE.setSeed(seme);
    }

    /**
     * I prossimi lanci danno questi risultati, in ordine: un intero per ogni {@code tira...}, un double in [0, 1)
     * per ogni {@link #probabilita()}. Finiti i trucchi si torna al caso. Un risultato del tipo sbagliato o fuori
     * dall'intervallo del lancio è un errore: vuol dire che il codice lancia diversamente da come il test crede.
     * I lanci senza scelta (una faccia sola, min uguale a max) non consumano trucchi.
     */
    public static void trucca(Number... risultati) {
        for (Number risultato : risultati) {
            if (!(risultato instanceof Integer) && !(risultato instanceof Double)) {
                throw new IllegalArgumentException("Un trucco è un Integer o un Double, non " + risultato);
            }
            TRUCCHI.addLast(risultato);
        }
    }

    /**
     * Toglie i trucchi rimasti e torna al caso.
     */
    public static void ripristina() {
        TRUCCHI.clear();
    }

    /**
     * Quanti trucchi non sono ancora stati usati: in un test, zero vuol dire che si è lanciato quanto previsto.
     */
    public static int trucchiRimasti() {
        return TRUCCHI.size();
    }

    /**
     * Un valore casuale in [0, 1), da confrontare con una probabilità: {@code Dado.probabilita() < 0.05}.
     */
    public static double probabilita() {
        Number trucco = TRUCCHI.pollFirst();
        if (trucco == null) {
            return SORGENTE.nextDouble();
        }
        if (!(trucco instanceof Double) || trucco.doubleValue() < 0 || trucco.doubleValue() >= 1) {
            throw new IllegalStateException("Trucco " + trucco + " non valido per una probabilità in [0, 1)");
        }
        return trucco.doubleValue();
    }

    private static int intero(int min, int max) {
        Number trucco = TRUCCHI.pollFirst();
        if (trucco == null) {
            return SORGENTE.nextInt(max - min + 1) + min;
        }
        if (!(trucco instanceof Integer) || trucco.intValue() < min || trucco.intValue() > max) {
            throw new IllegalStateException("Trucco " + trucco + " non valido per un lancio da " + min + " a " + max);
        }
        return trucco.intValue();
    }


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
        return intero(min, max);
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
        return intero(min, max);
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
