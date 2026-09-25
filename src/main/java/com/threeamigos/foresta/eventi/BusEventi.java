package com.threeamigos.foresta.eventi;

import javax.swing.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Un Bus Eventi sincrono
 * Chi deve pubblicare chiama pubblica(evento); chi si vuole iscrivere chiama iscriviti(Type.class, gestore).
 * La consegna avviene sull'Event Dispatch Thread: subito se si pubblica già da lì, altrimenti accodata.
 * I test possono sostituire la consegna (vedi {@link #impostaConsegna}) per riceverla subito sul proprio thread.
 *
 * @author Stefano Reksten
 */
public class BusEventi {

    private static final Map<Class<?>, List<Consumer<Object>>> sottoscrittori = new ConcurrentHashMap<>();

    /**
     * La consegna del gioco: sull'Event Dispatch Thread, subito se si è già lì.
     */
    public static final Consumer<Runnable> CONSEGNA_SU_EDT = consegna -> {
        if (SwingUtilities.isEventDispatchThread()) {
            consegna.run();
        } else {
            SwingUtilities.invokeLater(consegna);
        }
    };

    private static volatile Consumer<Runnable> consegna = CONSEGNA_SU_EDT;

    private BusEventi() {
    }

    @SuppressWarnings("unchecked")
    public static <T> void iscriviti(Class<T> eventType, Consumer<T> listener) {
        sottoscrittori
                .computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add((Consumer<Object>) listener);
    }

    public static <T> void cancellati(Class<T> eventType, Consumer<T> listener) {
        List<Consumer<Object>> list = sottoscrittori.get(eventType);
        if (list != null) {
            list.remove(listener);
        }
    }

    public static void pubblica(Object evento) {
        List<Consumer<Object>> list = sottoscrittori.get(evento.getClass());
        if (list == null || list.isEmpty()) {
            return;
        }
        consegna.accept(() -> list.forEach(c -> c.accept(evento)));
    }

    /**
     * Sostituisce il modo in cui gli eventi arrivano agli iscritti. Nei test {@code Runnable::run} li consegna
     * subito sul thread che pubblica, come succede nel gioco, dove tutto gira sull'Event Dispatch Thread.
     */
    public static void impostaConsegna(Consumer<Runnable> nuovaConsegna) {
        consegna = nuovaConsegna == null ? CONSEGNA_SU_EDT : nuovaConsegna;
    }

    /**
     * Toglie tutti gli iscritti. Serve ai test, che ricostruiscono una partita da zero a ogni prova.
     */
    public static void azzera() {
        sottoscrittori.clear();
    }
}
