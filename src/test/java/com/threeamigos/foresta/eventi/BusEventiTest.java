package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.eventi.interni.InternoException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusEventiTest {

    @BeforeEach
    void consegnaImmediata() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
    }

    @AfterEach
    void ripristina() {
        BusEventi.azzera();
        BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_EDT);
    }

    @Test
    void unIscrittoCheFallisceNonTogliELEventoAgliAltri() {
        List<String> ricevuti = new ArrayList<>();
        List<InternoException> errori = new ArrayList<>();
        BusEventi.iscriviti(String.class, s -> {
            throw new IllegalStateException("guasto");
        });
        BusEventi.iscriviti(String.class, ricevuti::add);
        BusEventi.iscriviti(InternoException.class, errori::add);

        BusEventi.pubblica("ciao");

        assertEquals(1, ricevuti.size());
        assertEquals(1, errori.size());
        assertEquals("guasto", errori.get(0).getException().getMessage());
    }

    @Test
    void unIscrittoAgliErroriCheFallisceNonCreaUnCiclo() {
        BusEventi.iscriviti(InternoException.class, e -> {
            throw new IllegalStateException("anche lui");
        });
        BusEventi.iscriviti(String.class, s -> {
            throw new IllegalStateException("guasto");
        });
        BusEventi.pubblica("ciao");
    }
}
