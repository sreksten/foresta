package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.modellodati.Notizia;
import com.threeamigos.foresta.modellodati.NotizieMD;
import com.threeamigos.foresta.tipi.Comando;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lo storico di quel che il giocatore ha visto: le notizie delle locande arrivano da chi le produce, i messaggi di
 * testo dal bus; più recente in testa, e oltre il limite si scarta il più vecchio.
 */
class NotizieTest {

    @Test
    void leNotizieSiAggiungonoInTestaEOltreIlLimiteSiScartanoLePiuVecchie() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(5)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            for (int i = 1; i <= NotizieMD.MASSIMO_NOTIZIE_RICORDATE + 3; i++) {
                Notizie.aggiungiNotizia(new Notizia("N" + i, "Titolo " + i + " - Corpo"));
            }
            assertEquals(NotizieMD.MASSIMO_NOTIZIE_RICORDATE, Notizie.getUltimeNotizie().size());
            assertEquals("N" + (NotizieMD.MASSIMO_NOTIZIE_RICORDATE + 3), Notizie.getUltimeNotizie().get(0).getId());
            assertEquals("N4", Notizie.getUltimeNotizie().get(NotizieMD.MASSIMO_NOTIZIE_RICORDATE - 1).getId());
        }
    }

    @Test
    void iMessaggiDiTestoVengonoDalBus() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(5)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            BusEventi.pubblica(new NotificaTestoFrase("Una frase di prova."));
            assertTrue(Notizie.getUltimiMessaggi().stream().anyMatch(m -> m.getTesto().equals("Una frase di prova.")));
            assertEquals("Una frase di prova.", Notizie.getUltimiMessaggi().get(0).getTesto(), "il più recente è in testa");
        }
    }
}
