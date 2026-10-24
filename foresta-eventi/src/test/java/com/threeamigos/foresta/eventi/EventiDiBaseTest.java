package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.eventi.interni.InternoErrore;
import com.threeamigos.foresta.eventi.interni.InternoException;
import com.threeamigos.foresta.eventi.interni.InternoMessaggio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventiDiBaseTest {

    @AfterEach
    void ripristina() {
        BusEventi.impostaConsegna(null);
        BusEventi.azzera();
    }

    @Test
    void unEventoHaIlSuoTipoEUnIdentificativoCheNonCambia() {
        EventoBase evento = new EventoBase(TipoEvento.INTERNO_ECCEZIONE);
        assertEquals(TipoEvento.INTERNO_ECCEZIONE, evento.getTipoEvento());
        assertEquals(evento.getUuid(), evento.getUuid());
        assertNotEquals(evento.getUuid(), new EventoBase(TipoEvento.INTERNO_ECCEZIONE).getUuid());
    }

    @Test
    void ilMessaggioDiUnErroreSenzaMessaggioEQuelloDellEccezione() {
        Exception causa = new IllegalStateException("guasto");
        InternoException senzaMessaggio = new InternoException(causa);
        assertEquals("guasto", senzaMessaggio.getMessaggio());
        assertSame(causa, senzaMessaggio.getException());
        InternoException conMessaggio = new InternoException("durante la prova", causa);
        assertEquals("durante la prova", conMessaggio.getMessaggio());
        assertEquals(TipoEvento.INTERNO_ECCEZIONE, conMessaggio.getTipoEvento());
    }

    @Test
    void togliereUnIscrittoSenzaIscrittiNonFaNiente() {
        BusEventi.cancellati(InternoException.class, e -> { });
    }

    @Test
    void pubblicareDopoAverToltoTuttiGliIscrittiNonConsegnaNiente() {
        List<InternoException> ricevuti = new ArrayList<>();
        Runnable togli = BusEventi.iscriviti(InternoException.class, ricevuti::add);
        togli.run();
        BusEventi.pubblica(new InternoException(new RuntimeException("x")));
        assertTrue(ricevuti.isEmpty());
    }

    @Test
    void pubblicareUnTipoSenzaMaiIscrittiNonFaNiente() {
        BusEventi.pubblica(new Object());
    }

    @Test
    void laConsegnaSiPuoSostituireERimettere() {
        List<String> consegne = new ArrayList<>();
        BusEventi.impostaConsegna(operazione -> {
            consegne.add("mia");
            operazione.run();
        });
        List<InternoException> ricevuti = new ArrayList<>();
        BusEventi.iscriviti(InternoException.class, ricevuti::add);
        BusEventi.pubblica(new InternoException(new RuntimeException("x")));
        assertEquals(1, consegne.size());
        assertEquals(1, ricevuti.size());
        BusEventi.impostaConsegna(null);
        BusEventi.pubblica(new InternoException(new RuntimeException("y")));
        assertEquals(1, consegne.size());
        assertEquals(2, ricevuti.size());
    }

    @Test
    void unErroreEUnMessaggioPortanoIlLoroTesto() {
        InternoErrore errore = new InternoErrore("qualcosa non va");
        assertEquals("qualcosa non va", errore.getMessaggio());
        assertEquals("qualcosa non va", errore.messaggio);
        assertEquals(TipoEvento.INTERNO_ERRORE, errore.getTipoEvento());
        InternoMessaggio messaggio = new InternoMessaggio("da registrare");
        assertEquals("da registrare", messaggio.getMessaggioInterno());
        assertEquals(TipoEvento.INTERNO_MESSAGGIO, messaggio.getTipoEvento());
    }
}
