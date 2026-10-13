package com.threeamigos.foresta.strumenti;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemporizzatoreJ2SETest {

    // Un thread principale di prova, che accoda le operazioni come l'Event Dispatch Thread di Swing
    private static final ExecutorService THREAD_PRINCIPALE = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "thread-principale-di-prova");
        thread.setDaemon(true);
        return thread;
    });

    private static void eseguiESpetta(Runnable operazione) throws Exception {
        THREAD_PRINCIPALE.submit(operazione).get();
    }

    @BeforeAll
    static void usaUnThreadPrincipaleDiProva() {
        ThreadPrincipale.imposta(new ThreadPrincipale.Esecutore() {
            @Override
            public boolean eIlThreadCorrente() {
                return Thread.currentThread().getName().equals("thread-principale-di-prova");
            }

            @Override
            public void accoda(Runnable operazione) {
                THREAD_PRINCIPALE.execute(operazione);
            }
        });
    }

    @AfterAll
    static void tornaAllEsecuzioneImmediata() {
        ThreadPrincipale.imposta(null);
    }

    @Test
    void gliImpulsiGiaAccodatiNonArrivanoDopoTermina() throws Exception {
        TemporizzatoreJ2SE temporizzatore = new TemporizzatoreJ2SE();
        AtomicInteger impulsi = new AtomicInteger();
        temporizzatore.setTemporizzabile(impulsi::incrementAndGet);
        eseguiESpetta(() -> {
            temporizzatore.inizia(2);
            // Il thread principale e' occupato: intanto gli impulsi si accodano
            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            temporizzatore.termina();
        });
        // Lascia eseguire tutto cio' che si e' accodato
        Thread.sleep(30);
        eseguiESpetta(() -> { });
        assertEquals(0, impulsi.get());
    }

    @Test
    void finoATerminaGliImpulsiArrivano() throws Exception {
        TemporizzatoreJ2SE temporizzatore = new TemporizzatoreJ2SE();
        AtomicInteger impulsi = new AtomicInteger();
        temporizzatore.setTemporizzabile(impulsi::incrementAndGet);
        eseguiESpetta(() -> temporizzatore.inizia(5));
        Thread.sleep(80);
        eseguiESpetta(temporizzatore::termina);
        assertTrue(impulsi.get() > 0);
    }
}
