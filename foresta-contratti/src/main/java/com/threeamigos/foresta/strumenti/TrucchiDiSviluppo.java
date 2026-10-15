package com.threeamigos.foresta.strumenti;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

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

    private final Map<ContestoLancio, Integer> numeri = new EnumMap<>(ContestoLancio.class);
    private final Map<ContestoLancio, Object> elementi = new EnumMap<>(ContestoLancio.class);

    private TrucchiDiSviluppo() {
    }

    public static TrucchiDiSviluppo istanza() {
        return ISTANZA;
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

    public synchronized void spegni(ContestoLancio contesto) {
        numeri.remove(contesto);
        elementi.remove(contesto);
    }

    public synchronized boolean isAcceso(ContestoLancio contesto) {
        return numeri.containsKey(contesto) || elementi.containsKey(contesto);
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

    private static void controllaTipo(ContestoLancio contesto, boolean scelta) {
        if (contesto.isScelta() != scelta) {
            throw new IllegalArgumentException(
                    contesto + (contesto.isScelta() ? " sceglie un elemento, non un numero" : " dà un numero, non sceglie un elemento"));
        }
    }
}
