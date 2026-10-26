package com.threeamigos.foresta.notizie;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.modellodati.NotiziaMD;
import com.threeamigos.foresta.modellodati.NotizieMD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lo storico di quel che il giocatore ha visto: le notizie delle locande arrivano da chi le produce, i messaggi di
 * testo dal bus; più recente in testa, e oltre il limite si scarta il più vecchio.
 */
class NotizieTest {

    @BeforeEach
    void preparaIlBus() {
        NotizieMD dati = new NotizieMD();
        Notizie.setFornitoreDiDati(() -> dati);
        BusEventi.azzera();
        BusEventi.impostaConsegna(Runnable::run);
        Notizie.registrati();
    }

    @AfterEach
    void azzeraIlBus() {
        BusEventi.azzera();
    }

    @Test
    void leNotizieSiAggiungonoInTestaEOltreIlLimiteSiScartanoLePiuVecchie() {
        for (int i = 1; i <= NotizieMD.MASSIMO_NOTIZIE_RICORDATE + 3; i++) {
            Notizie.aggiungiNotizia(new NotiziaMD("N" + i, "Titolo " + i + " - Corpo"));
        }
        assertEquals(NotizieMD.MASSIMO_NOTIZIE_RICORDATE, Notizie.getUltimeNotizie().size());
        assertEquals("N" + (NotizieMD.MASSIMO_NOTIZIE_RICORDATE + 3), Notizie.getUltimeNotizie().get(0).getId());
        assertEquals("N4", Notizie.getUltimeNotizie().get(NotizieMD.MASSIMO_NOTIZIE_RICORDATE - 1).getId());
    }

    @Test
    void iMessaggiDiTestoVengonoDalBus() {
        BusEventi.pubblica(new NotificaTestoFrase("Una frase di prova."));
        assertTrue(Notizie.getUltimiMessaggi().stream().anyMatch(m -> m.getTesto().equals("Una frase di prova.")));
        assertEquals("Una frase di prova.", Notizie.getUltimiMessaggi().get(0).getTesto(), "il più recente è in testa");
    }

    @Test
    void unParagrafoSiRicordaComeInizioDiParagrafo() {
        BusEventi.pubblica(new NotificaTestoParagrafo("Un nuovo paragrafo."));
        assertEquals("Un nuovo paragrafo.", Notizie.getUltimiMessaggi().get(0).getTesto());
        assertTrue(Notizie.getUltimiMessaggi().get(0).isParagrafo());
    }

    @Test
    void senzaUnFornitoreNonSaDoveLeggere() {
        Notizie.setFornitoreDiDati(null);
        assertThrows(IllegalStateException.class, Notizie::getUltimeNotizie);
    }
}
