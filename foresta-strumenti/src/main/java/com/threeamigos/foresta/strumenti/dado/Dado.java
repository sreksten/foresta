package com.threeamigos.foresta.strumenti.dado;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Simula un lancio di dado. Tutto il caso delle decisioni di gioco passa da qui, così nei test si può:
 * <ul>
 *     <li>fissare il seme ({@link #impostaSeme}) per rendere ripetibile una partita intera;</li>
 *     <li>truccare i prossimi lanci ({@link #trucca}) per provare un caso preciso.</li>
 * </ul>
 * I lanci importanti hanno anche un contesto ({@link ContestoLancio}): in modalità di prova un
 * {@link TruccatoreDiDadi} può decidere il risultato di un contesto, finché non si spegne il trucco. La coda dei test
 * ha la precedenza.
 * Il generatore di artefatti e le grammatiche (GrammarBean) usano direttamente {@link #sorgente()}: seguono il seme
 * ma non consumano i lanci truccati, che restano per le decisioni.
 *
 * @author Stefano Reksten
 */
public final class Dado {

    // Una classe di soli metodi statici: non si istanzia
    private Dado() {
    }

    private static final Random SORGENTE = new Random();

    // I prossimi risultati truccati, in ordine: Integer per i lanci, Double per le probabilità
    private static final Deque<Number> TRUCCHI = new ConcurrentLinkedDeque<>();

    // Chi decide i lanci con contesto: solo in modalità di prova
    private static volatile TruccatoreDiDadi truccatore;

    /**
     * Chi decide i lanci con contesto al posto del caso; null per toglierlo.
     */
    public static void impostaTruccatore(TruccatoreDiDadi truccatoreDiDadi) {
        truccatore = truccatoreDiDadi;
    }

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
            // Con un intervallo enorme (per esempio tutti gli int) max - min + 1 uscirebbe dagli int
            long ampiezza = (long) max - min + 1;
            if (ampiezza <= Integer.MAX_VALUE) {
                return SORGENTE.nextInt((int) ampiezza) + min;
            }
            return (int) (min + (long) (SORGENTE.nextDouble() * ampiezza));
        }
        if (!(trucco instanceof Integer) || trucco.intValue() < min || trucco.intValue() > max) {
            throw new IllegalStateException("Trucco " + trucco + " non valido per un lancio da " + min + " a " + max);
        }
        return trucco.intValue();
    }

    /**
     * Simula un lancio di dado a N facce con N &gt;= 1.
     * <p> Questa funzione è utile quando occorre passare la dimensione di un array o di una lista per recuperare
     * un elemento casuale.
     * Normalmente un dado non ha una faccia sola, ma in caso non si sappia di preciso quale sarà il numero di facce
     * perché per esempio si sta filtrando su una lista, si può usare questo metodo.
     * L'unico controllo da fare è che la lista non sia vuota (basta che N sia &gt; 0)
     * @param N il numero di facce del dado
     * @return il risultato del lancio, da 1 a N inclusi
     */
    public static int tiraAncheAUnaFaccia(int N) {
        if (N < 1) {
            throw new IllegalArgumentException("Dado.tiraAncheAUnaFaccia(" + N + "): il numero di facce deve essere maggiore di zero");
        }
        if (N == 1) {
            return 1;
        }
        return tira(1, N);
    }

    /**
     * Simula un lancio di dado a (max - min + 1) facce con numero minimo &gt;= 1.
     * <p>L'unico controllo è che min sia &lt;= max
     * @param min il valore minimo
     * @param max il valore massimo
     * @return il risultato del lancio, da 1 a N inclusi
     */
    public static int tiraAncheSenzaRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Dado.tiraAncheSenzaRange(" + min + ", " + max + "): il valore minimo deve essere minore o uguale al valore massimo");
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
            throw new IllegalArgumentException("Dado.tira(" + N + "): il numero di facce deve essere maggiore di 1");
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
            throw new IllegalArgumentException("Dado.tira(" + min + ", " + max + "): il valore minimo deve essere minore del valore massimo");
        }
        return intero(min, max);
    }

    /**
     * Come {@link #tira(int, int)}, per un contesto che chi sviluppa può truccare. Se la coda dei trucchi dei test non è
     * vuota vale quella; altrimenti, se il truccatore ha un risultato dentro l'intervallo, quello; altrimenti il caso.
     */
    public static int tira(ContestoLancio contesto, int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("Dado.tira(" + contesto + ", " + min + ", " + max + "): il valore minimo deve essere minore del valore massimo");
        }
        if (TRUCCHI.isEmpty()) {
            TruccatoreDiDadi t = truccatore;
            if (t != null) {
                OptionalInt voluto = t.lancio(contesto, min, max);
                if (voluto.isPresent() && voluto.getAsInt() >= min && voluto.getAsInt() <= max) {
                    return voluto.getAsInt();
                }
            }
        }
        return intero(min, max);
    }

    /**
     * Come {@link #tira(int)}, per un contesto: da 1 a N inclusi.
     */
    public static int tira(ContestoLancio contesto, int N) {
        if (N <= 1) {
            throw new IllegalArgumentException("Dado.tira(" + contesto + ", " + N + "): il numero di facce deve essere maggiore di 1");
        }
        return tira(contesto, 1, N);
    }

    /**
     * Come {@link #tiraAncheAUnaFaccia(int)}, per un contesto: con una faccia sola non si sceglie nulla e non si
     * consulta il truccatore.
     */
    public static int tiraAncheAUnaFaccia(ContestoLancio contesto, int N) {
        if (N < 1) {
            throw new IllegalArgumentException("Dado.tiraAncheAUnaFaccia(" + contesto + ", " + N + "): il numero di facce deve essere maggiore di zero");
        }
        if (N == 1) {
            return 1;
        }
        return tira(contesto, 1, N);
    }

    /**
     * Un elemento a caso fra quelli ammessi (che non possono essere vuoti), a meno che il truccatore ne voglia uno
     * ammesso per quel contesto (la coda dei trucchi dei test ha la precedenza, come per i lanci).
     */
    public static <T> T scegli(ContestoLancio contesto, List<T> ammessi) {
        if (ammessi.isEmpty()) {
            throw new IllegalArgumentException("Dado.scegli(" + contesto + "): non c'è niente da scegliere");
        }
        Optional<T> voluto = sceltaVoluta(contesto, ammessi);
        if (voluto.isPresent()) {
            return voluto.get();
        }
        return ammessi.get(tiraAncheAUnaFaccia(ammessi.size()) - 1);
    }

    /**
     * L'elemento che il truccatore vuole per il contesto, se ne vuole uno fra gli ammessi; altrimenti niente, e chi
     * chiama procede come sempre (senza tirare nulla). Con la coda dei trucchi dei test non vuota è sempre vuoto.
     */
    public static <T> Optional<T> sceltaVoluta(ContestoLancio contesto, List<T> ammessi) {
        TruccatoreDiDadi t = truccatore;
        if (t == null || !TRUCCHI.isEmpty()) {
            return Optional.empty();
        }
        return t.scelta(contesto, ammessi).filter(ammessi::contains);
    }

}
