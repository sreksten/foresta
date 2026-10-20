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
        BusEventi.impostaConsegna(BusEventi.CONSEGNA_SU_THREAD_PRINCIPALE);
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

    @Test
    void laIscrizioneSiToglieAncheSeEUnaLambda() {
        List<String> ricevuti = new ArrayList<>();
        Runnable togli = BusEventi.iscriviti(String.class, s -> ricevuti.add(s));

        BusEventi.pubblica("uno");
        togli.run();
        BusEventi.pubblica("due");

        assertEquals(1, ricevuti.size());
        assertEquals("uno", ricevuti.get(0));
    }

    @Test
    void ognEventoHaIlSuoUuidChiSiGeneraAllaPrimaRichiesta() {
        EventoBase uno = new EventoBase(TipoEvento.INTERNO_ERRORE) { };
        EventoBase altro = new EventoBase(TipoEvento.INTERNO_ERRORE) { };

        String primo = uno.getUuid();

        assertEquals(primo, uno.getUuid(), "è sempre lo stesso per lo stesso evento");
        org.junit.jupiter.api.Assertions.assertNotEquals(primo, altro.getUuid());
    }

    @Test
    void unErrorePureNonTogliELEventoAgliAltriEFinisceSulBus() {
        List<String> ricevuti = new ArrayList<>();
        List<InternoException> errori = new ArrayList<>();
        BusEventi.iscriviti(String.class, s -> {
            throw new NoClassDefFoundError("classe mancante");
        });
        BusEventi.iscriviti(String.class, ricevuti::add);
        BusEventi.iscriviti(InternoException.class, errori::add);

        BusEventi.pubblica("ciao");

        assertEquals(1, ricevuti.size(), "gli altri iscritti ricevono l'evento");
        assertEquals(1, errori.size());
        assertEquals(NoClassDefFoundError.class, errori.get(0).getException().getCause().getClass());
    }

    @Test
    void glErroriDellaMacchinaVirtualeNonSiInghiottono() {
        BusEventi.iscriviti(String.class, s -> {
            throw new OutOfMemoryError("memoria finita");
        });

        org.junit.jupiter.api.Assertions.assertThrows(OutOfMemoryError.class, () -> BusEventi.pubblica("ciao"));
    }
}
