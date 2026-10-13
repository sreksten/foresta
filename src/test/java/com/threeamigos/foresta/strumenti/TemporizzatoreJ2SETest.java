package com.threeamigos.foresta.strumenti;

import com.threeamigos.foresta.ui.EsecutoreSwing;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemporizzatoreJ2SETest {

    @BeforeAll
    static void usaIlThreadDiSwing() {
        ThreadPrincipale.imposta(new EsecutoreSwing());
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
        SwingUtilities.invokeAndWait(() -> {
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
        SwingUtilities.invokeAndWait(() -> { });
        assertEquals(0, impulsi.get());
    }

    @Test
    void finoATerminaGliImpulsiArrivano() throws Exception {
        TemporizzatoreJ2SE temporizzatore = new TemporizzatoreJ2SE();
        AtomicInteger impulsi = new AtomicInteger();
        temporizzatore.setTemporizzabile(impulsi::incrementAndGet);
        SwingUtilities.invokeAndWait(() -> temporizzatore.inizia(5));
        Thread.sleep(80);
        SwingUtilities.invokeAndWait(temporizzatore::termina);
        assertTrue(impulsi.get() > 0);
    }
}
