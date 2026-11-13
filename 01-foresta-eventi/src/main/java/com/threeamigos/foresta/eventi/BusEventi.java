package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.strumenti.temporizzatore.ThreadPrincipale;
import com.threeamigos.foresta.strumenti.logger.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Il bus degli eventi: il modo in cui le parti del gioco si parlano senza conoscersi (lo schema «publish/subscribe»).
 * Chi vuole sapere quando succede qualcosa si iscrive al tipo di evento, chi lo fa succedere lo pubblica:
 * <pre>
 * BusEventi.iscriviti(NotificaGlobale.class, evento -&gt; mostra(evento.getMessaggio()));   // chi ascolta
 * BusEventi.pubblica(new NotificaGlobale("Nuova missione!", "Trova il drago"));          // chi parla
 * </pre>
 * Chi pubblica non sa chi ascolta, e chi ascolta non sa chi ha pubblicato: per questo il motore può parlare con
 * un'interfaccia che non conosce.
 * <p>
 * La consegna avviene sul thread principale (vedi {@link ThreadPrincipale}): subito, prima che {@code pubblica} finisca,
 * se si pubblica già da lì (è il caso normale); accodata se si pubblica da un altro thread.
 * I test possono sostituire la consegna (vedi {@link #impostaConsegna}) per riceverla subito sul proprio thread.
 *
 * @author Stefano Reksten
 */
public class BusEventi {

    // Per ogni classe di evento, chi la ascolta. Un Consumer è una funzione che riceve un valore e non restituisce niente
    // (la lambda passata a iscriviti). Le collezioni "concurrent" e "copy on write" si possono usare da più thread, e
    // permettono a un iscritto di iscriversi o cancellarsi mentre il bus sta consegnando, senza errori
    private static final Map<Class<?>, List<Consumer<Object>>> sottoscrittori = new ConcurrentHashMap<>();

    /**
     * La consegna del gioco: sul thread principale, subito se si è già lì.
     */
    public static final Consumer<Runnable> CONSEGNA_SU_THREAD_PRINCIPALE = ThreadPrincipale::esegui;

    private static volatile Consumer<Runnable> consegna = CONSEGNA_SU_THREAD_PRINCIPALE;

    // Le iscrizioni fatte su questo thread mentre si costruisce qualcosa che poi si butterà via (vedi iscrizioniDurante).
    // Un ThreadLocal è una variabile che ha un valore diverso per ogni thread: la raccolta di un thread non vede le
    // iscrizioni fatte da un altro
    private static final ThreadLocal<List<Runnable>> raccolta = new ThreadLocal<>();

    /**
     * Registra un gestore per gli eventi di quel tipo: ne bastano quelli della classe indicata, non le sue sottoclassi.
     *
     * @return come togliere l'iscrizione: funziona anche per una lambda, che {@link #cancellati} non saprebbe ritrovare
     * a chi non ne tiene il riferimento
     */
    @SuppressWarnings("unchecked")
    public static <T> Runnable iscriviti(Class<T> tipoDiEvento, Consumer<T> iscritto) {
        // computeIfAbsent crea la lista degli iscritti a quel tipo la prima volta che serve. Il cast a Consumer<Object> è
        // sicuro: a questo iscritto arriveranno solo eventi della classe T (vedi pubblica), ma la mappa non sa dirlo al
        // compilatore, da qui il @SuppressWarnings
        sottoscrittori
                .computeIfAbsent(tipoDiEvento, k -> new CopyOnWriteArrayList<>())
                .add((Consumer<Object>) iscritto);
        Runnable cancellazione = () -> cancellati(tipoDiEvento, iscritto);
        List<Runnable> inRaccolta = raccolta.get();
        if (inRaccolta != null) {
            inRaccolta.add(cancellazione);
        }
        return cancellazione;
    }

    /**
     * Fa quell'azione e dice come togliere tutte le iscrizioni che ha fatto, su questo thread: per costruire qualcosa che si iscrive in tanti punti
     * (lo schermo del gioco, con i suoi riquadri) e che più avanti si butta via per costruirne un altro.
     */
    public static List<Runnable> iscrizioniDurante(Runnable azione) {
        List<Runnable> precedente = raccolta.get();
        List<Runnable> iscrizioni = new java.util.ArrayList<>();
        raccolta.set(iscrizioni);
        try {
            azione.run();
        } finally {
            raccolta.set(precedente);
        }
        return iscrizioni;
    }

    /**
     * Toglie un gestore registrato con {@link #iscriviti}.
     */
    public static <T> void cancellati(Class<T> tipoDiEvento, Consumer<T> iscritto) {
        List<Consumer<Object>> iscritti = sottoscrittori.get(tipoDiEvento);
        if (iscritti != null) {
            iscritti.remove(iscritto);
        }
    }

    /**
     * Consegna l'evento a tutti gli iscritti al suo tipo, sul thread principale: subito se si è già lì, altrimenti in coda.
     * Senza iscritti non succede niente.
     */
    public static void pubblica(Object evento) {
        // Si cerca per la classe esatta dell'evento: chi si iscrive a una superclasse non riceve le sottoclassi
        List<Consumer<Object>> iscritti = sottoscrittori.get(evento.getClass());
        if (iscritti == null || iscritti.isEmpty()) {
            return;
        }
        consegna.accept(() -> iscritti.forEach(iscritto -> consegnaA(iscritto, evento)));
    }

    /**
     * Consegna a un solo iscritto: se fallisce, gli altri ricevono comunque l'evento (la UI resta aggiornata, le
     * notizie registrate) e chi ha pubblicato non si trova l'eccezione a metà di una transizione. L'errore
     * finisce nel log e sul bus come InternoException (la UI lo dice al giocatore), tranne quando a fallire è proprio
     * chi ascolta quelle. Vale anche per gli {@link Error} (una classe che non si carica, un'asserzione): solo quelli della macchina
     * virtuale, come la memoria finita, non si fermano qui.
     */
    private static void consegnaA(Consumer<Object> iscritto, Object evento) {
        try {
            iscritto.accept(evento);
        } catch (VirtualMachineError e) {
            throw e;
        } catch (RuntimeException | Error e) {
            // Un Error non è una Exception: per il registro e per l'evento lo si avvolge in una RuntimeException
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
