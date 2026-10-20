package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.eventi.interni.InternoException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Un errore interno si dice al giocatore, una volta sola se si ripete uguale.
 */
class SegnalatoreDegliErroriTest {

    @Test
    void ilMessaggioDiceCheCosaEAndatoStorto() {
        List<String> messaggi = new ArrayList<>();
        SegnalatoreDegliErrori segnalatore = new SegnalatoreDegliErrori(messaggi::add);

        segnalatore.segnala(new InternoException("Durante la consegna di ComandoDiGioco", new IllegalStateException("guasto")));

        assertEquals(1, messaggi.size());
        assertTrue(messaggi.get(0).startsWith("Errore interno: Durante la consegna di ComandoDiGioco."), messaggi.get(0));
    }

    @Test
    void senzaMessaggioSiDiceLaClasseDellEccezione() {
        List<String> messaggi = new ArrayList<>();
        SegnalatoreDegliErrori segnalatore = new SegnalatoreDegliErrori(messaggi::add);

        segnalatore.segnala(new InternoException(new NullPointerException()));

        assertTrue(messaggi.get(0).startsWith("Errore interno: NullPointerException."), messaggi.get(0));
    }

    @Test
    void lStessoErroreDiFileSiDiceUnaVoltaSola() {
        List<String> messaggi = new ArrayList<>();
        SegnalatoreDegliErrori segnalatore = new SegnalatoreDegliErrori(messaggi::add);
        InternoException errore = new InternoException("guasto", new IllegalStateException());

        segnalatore.segnala(errore);
        segnalatore.segnala(errore);
        segnalatore.segnala(new InternoException("un altro guasto", new IllegalStateException()));
        segnalatore.segnala(errore);

        assertEquals(3, messaggi.size(), "i ripetuti di fila contano una volta, uno diverso e poi di nuovo il primo sì");
    }
}
