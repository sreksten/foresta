package com.threeamigos.foresta.temporizzatore;

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

    @Test
    void dopoSospendiNonArrivanoImpulsiEConRiprendiRipartono() throws Exception {
        TemporizzatoreJ2SE temporizzatore = new TemporizzatoreJ2SE();
        AtomicInteger impulsi = new AtomicInteger();
        temporizzatore.setTemporizzabile(impulsi::incrementAndGet);
        eseguiESpetta(() -> temporizzatore.inizia(5));
        attendi(() -> impulsi.get() >= 2);

        eseguiESpetta(temporizzatore::sospendi);
        // Lascia eseguire cio' che si era gia' accodato, che pero' non deve contare
        eseguiESpetta(() -> { });
        int allaSospensione = impulsi.get();
        Thread.sleep(80);
        assertEquals(allaSospensione, impulsi.get(), "sospeso: nessun impulso");

        eseguiESpetta(temporizzatore::riprendi);
        attendi(() -> impulsi.get() >= allaSospensione + 2);
        eseguiESpetta(temporizzatore::termina);
    }

    @Test
    void unInizioDuranteLaSospensioneParteSoloAllaRipresa() throws Exception {
        TemporizzatoreJ2SE temporizzatore = new TemporizzatoreJ2SE();
        AtomicInteger impulsi = new AtomicInteger();
        temporizzatore.setTemporizzabile(impulsi::incrementAndGet);
        eseguiESpetta(temporizzatore::sospendi);

        eseguiESpetta(() -> temporizzatore.inizia(5));
        Thread.sleep(60);
        eseguiESpetta(() -> { });
        assertEquals(0, impulsi.get(), "in background un impulso non parte");

        eseguiESpetta(temporizzatore::riprendi);
        attendi(() -> impulsi.get() >= 2);
        eseguiESpetta(temporizzatore::termina);
    }

    @Test
    void dopoTerminaRiprendiNonFaRipartireNiente() throws Exception {
        TemporizzatoreJ2SE temporizzatore = new TemporizzatoreJ2SE();
        AtomicInteger impulsi = new AtomicInteger();
        temporizzatore.setTemporizzabile(impulsi::incrementAndGet);
        eseguiESpetta(() -> temporizzatore.inizia(5));
        attendi(() -> impulsi.get() >= 1);

        eseguiESpetta(temporizzatore::sospendi);
        eseguiESpetta(temporizzatore::termina);
        eseguiESpetta(() -> { });
        int allaFine = impulsi.get();
        eseguiESpetta(temporizzatore::riprendi);
        Thread.sleep(60);
        eseguiESpetta(() -> { });
        assertEquals(allaFine, impulsi.get(), "terminato: niente da riprendere");
    }

    // Aspetta che la condizione si avveri, fino a due secondi
    private static void attendi(java.util.function.BooleanSupplier condizione) throws InterruptedException {
        long limite = System.nanoTime() + 2_000_000_000L;
        while (!condizione.getAsBoolean()) {
            if (System.nanoTime() > limite) {
                throw new AssertionError("la condizione non si e' avverata in due secondi");
            }
            Thread.sleep(5);
        }
    }

    @Test
    void unImpulsoCheFallisceSempreSiFermaDopoQualcheErroreDiFila() throws Exception {
        java.util.List<com.threeamigos.foresta.eventi.interni.InternoException> errori = new java.util.concurrent.CopyOnWriteArrayList<>();
        com.threeamigos.foresta.eventi.BusEventi.iscriviti(com.threeamigos.foresta.eventi.interni.InternoException.class, errori::add);
        TemporizzatoreJ2SE temporizzatore = new TemporizzatoreJ2SE();
        AtomicInteger impulsi = new AtomicInteger();
        temporizzatore.setTemporizzabile(() -> {
            impulsi.incrementAndGet();
            throw new IllegalStateException("guasto");
        });

        temporizzatore.inizia(5);
        Thread.sleep(400);
        eseguiESpetta(() -> { });
        int dopoUnPo = impulsi.get();
        Thread.sleep(200);
        eseguiESpetta(() -> { });

        temporizzatore.termina();
        assertEquals(3, dopoUnPo, "dopo tre errori di fila gli impulsi si fermano");
        assertEquals(3, impulsi.get(), "e non ne arrivano altri");
        assertEquals(4, errori.size(), "tre errori più l'avviso che gli impulsi si sono fermati");
    }
}
