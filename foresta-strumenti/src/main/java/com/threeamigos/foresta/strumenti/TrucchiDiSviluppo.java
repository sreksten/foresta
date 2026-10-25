package com.threeamigos.foresta.strumenti;

import com.threeamigos.foresta.strumenti.dado.ContestoLancio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.strumenti.dado.TruccatoreDiDadi;

import java.util.*;

/**
 * I trucchi che chi sviluppa accende dalla finestra di prova: per ogni {@link ContestoLancio} un trucco permanente
 * (resta finché non lo si spegne) con il numero voluto, o con l'elemento voluto per i contesti che scelgono. Un trucco
 * il cui valore non è fra quelli possibili in quel lancio (un 5 per un lancio da 1 a 3) non si applica: quel lancio va
 * al caso.
 * <p>
 * Si installa con {@link #installa()}; lo stato può essere letto e scritto da qualunque thread.
 *
 * @author Stefano Reksten
 */
public final class TrucchiDiSviluppo implements TruccatoreDiDadi {

    private static final TrucchiDiSviluppo ISTANZA = new TrucchiDiSviluppo();

    /**
     * L'unica istanza dei trucchi.
     */
    public static TrucchiDiSviluppo istanza() {
        return ISTANZA;
    }

    private static void controllaTipo(ContestoLancio contesto, boolean scelta) {
        if (contesto.isScelta() != scelta) {
            throw new IllegalArgumentException(
                    contesto + (contesto.isScelta() ? " sceglie un elemento, non un numero" : " dà un numero, non sceglie un elemento"));
        }
    }

    private final Map<ContestoLancio, Integer> numeri = new EnumMap<>(ContestoLancio.class);
    private final Map<ContestoLancio, Object> elementi = new EnumMap<>(ContestoLancio.class);

    private TrucchiDiSviluppo() {
    }

    /**
     * Fa decidere a questi trucchi i lanci con contesto.
     */
    public void installa() {
        Dado.impostaTruccatore(this);
    }

    /**
     * Tutti i trucchi spenti.
     */
    public synchronized void spegniTutto() {
        numeri.clear();
        elementi.clear();
    }

    /**
     * Il contesto numerico dà sempre quel risultato (se è possibile in quel lancio).
     */
    public synchronized void accendiNumero(ContestoLancio contesto, int valore) {
        controllaTipo(contesto, false);
        numeri.put(contesto, valore);
    }

    /**
     * Il contesto di scelta dà sempre quell'elemento (se fra gli ammessi di quel lancio).
     */
    public synchronized void accendiElemento(ContestoLancio contesto, Object elemento) {
        controllaTipo(contesto, true);
        elementi.put(contesto, elemento);
    }

    /**
     * Spegne il trucco di quel contesto, numero o elemento che sia.
     */
    public synchronized void spegni(ContestoLancio contesto) {
        numeri.remove(contesto);
        elementi.remove(contesto);
    }

    /**
     * Se quel contesto ha un trucco acceso.
     */
    public synchronized boolean isAcceso(ContestoLancio contesto) {
        return numeri.containsKey(contesto) || elementi.containsKey(contesto);
    }

    /**
     * Il numero voluto dal trucco acceso su quel contesto, se ce n'è uno.
     */
    public synchronized OptionalInt getNumero(ContestoLancio contesto) {
        Integer valore = numeri.get(contesto);
        return valore == null ? OptionalInt.empty() : OptionalInt.of(valore);
    }

    /**
     * L'elemento voluto dal trucco acceso su quel contesto, se ce n'è uno.
     */
    public synchronized Optional<Object> getElemento(ContestoLancio contesto) {
        return Optional.ofNullable(elementi.get(contesto));
    }

    @Override
    public synchronized OptionalInt lancio(ContestoLancio contesto, int min, int max) {
        Integer valore = numeri.get(contesto);
        if (valore == null || valore < min || valore > max) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(valore);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T> Optional<T> scelta(ContestoLancio contesto, List<T> ammessi) {
        Object voluto = elementi.get(contesto);
        if (voluto == null || !ammessi.contains(voluto)) {
            return Optional.empty();
        }
        return Optional.of((T) voluto);
    }

}
