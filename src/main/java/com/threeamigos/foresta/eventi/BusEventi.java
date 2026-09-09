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
 * La consegna avviene sull'Event Dispatch Thread.
 *
 * @author Stefano Reksten
 */
public class BusEventi {

    private static final Map<Class<?>, List<Consumer<Object>>> sottoscrittori = new ConcurrentHashMap<>();

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
        if (SwingUtilities.isEventDispatchThread()) {
            list.forEach(c -> c.accept(evento));
        } else {
            SwingUtilities.invokeLater(() -> list.forEach(c -> c.accept(evento)));
        }
    }
}
