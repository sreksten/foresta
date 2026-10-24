package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.strumenti.logger.Logger;
import com.threeamigos.foresta.strumenti.ThreadPrincipale;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Un Bus Eventi sincrono
 * Chi deve pubblicare chiama pubblica(evento); chi si vuole iscrivere chiama iscriviti(Type.class, gestore).
 * La consegna avviene sul thread principale (vedi {@link ThreadPrincipale}): subito se si pubblica già da lì,
 * altrimenti accodata.
 * I test possono sostituire la consegna (vedi {@link #impostaConsegna}) per riceverla subito sul proprio thread.
 *
 * @author Stefano Reksten
 */
public class BusEventi {

    private static final Map<Class<?>, List<Consumer<Object>>> sottoscrittori = new ConcurrentHashMap<>();

    /**
     * La consegna del gioco: sul thread principale, subito se si è già lì.
     */
    public static final Consumer<Runnable> CONSEGNA_SU_THREAD_PRINCIPALE = ThreadPrincipale::esegui;

    private static volatile Consumer<Runnable> consegna = CONSEGNA_SU_THREAD_PRINCIPALE;

    /**
     * Registra un gestore per gli eventi di quel tipo: ne bastano quelli della classe indicata, non le sue sottoclassi.
     *
     * @return come togliere l'iscrizione: funziona anche per una lambda, che {@link #cancellati} non saprebbe ritrovare
     * a chi non ne tiene il riferimento
     */
    @SuppressWarnings("unchecked")
    public static <T> Runnable iscriviti(Class<T> eventType, Consumer<T> listener) {
        sottoscrittori
                .computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add((Consumer<Object>) listener);
        return () -> cancellati(eventType, listener);
    }

    /**
     * Toglie un gestore registrato con {@link #iscriviti}.
     */
    public static <T> void cancellati(Class<T> eventType, Consumer<T> listener) {
        List<Consumer<Object>> list = sottoscrittori.get(eventType);
        if (list != null) {
            list.remove(listener);
        }
    }

    /**
     * Consegna l'evento a tutti gli iscritti al suo tipo, sul thread principale: subito se si è già lì, altrimenti in coda.
     * Senza iscritti non succede niente.
     */
    public static void pubblica(Object evento) {
        List<Consumer<Object>> list = sottoscrittori.get(evento.getClass());
        if (list == null || list.isEmpty()) {
            return;
        }
        consegna.accept(() -> list.forEach(c -> consegnaA(c, evento)));
    }

    /**
     * Consegna a un solo iscritto: se fallisce, gli altri ricevono comunque l'evento (la UI resta aggiornata, le
     * notizie registrate) e chi ha pubblicato non si trova l'eccezione a meta' di una transizione. L'errore
     * finisce nel log e sul bus come InternoException (la UI lo dice al giocatore), tranne quando a fallire e' proprio
     * chi ascolta quelle. Vale anche per gli {@link Error} (una classe che non si carica, un'asserzione): solo quelli della macchina
     * virtuale, come la memoria finita, non si fermano qui.
     */
    private static void consegnaA(Consumer<Object> iscritto, Object evento) {
        try {
            iscritto.accept(evento);
        } catch (VirtualMachineError e) {
            throw e;
        } catch (RuntimeException | Error e) {
            Exception eccezione = e instanceof Exception ? (Exception) e : new RuntimeException(e);
            Logger.log(eccezione);
            if (!(evento instanceof InternoException)) {
                pubblica(new InternoException("Durante la consegna di " + evento.getClass().getSimpleName(), eccezione));
            }
        }
    }

    /**
     * Sostituisce il modo in cui gli eventi arrivano agli iscritti. Nei test {@code Runnable::run} li consegna
     * subito sul thread che pubblica, come succede nel gioco, dove tutto gira sul thread principale.
     */
    public static void impostaConsegna(Consumer<Runnable> nuovaConsegna) {
        consegna = nuovaConsegna == null ? CONSEGNA_SU_THREAD_PRINCIPALE : nuovaConsegna;
    }

    /**
     * Toglie tutti gli iscritti. Serve ai test, che ricostruiscono una partita da zero a ogni prova.
     */
    public static void azzera() {
        sottoscrittori.clear();
    }

    private BusEventi() {
    }

}
