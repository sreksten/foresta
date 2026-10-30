package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMissioneCompletata;
import com.threeamigos.foresta.eventi.notifiche.NotificaAggiornamentoStatoMissione;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.eventi.richieste.RichiestaSelezioneMissione;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;
import com.threeamigos.foresta.tipi.MomentoIntermezzo;
import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;
import com.threeamigos.foresta.tipi.TipoIntermezzo;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I contratti e i piccoli pezzi delle missioni che nessuna partita giocata attraversa da sola: gli eventi con i loro dati, la sfida a carta,
 * forbici e sasso fino in fondo, la proposta del sacerdote a gruppo pieno, gli intermezzi delle missioni in ogni momento.
 */
class PiccoliContrattiDelleMissioniTest {

    @BeforeEach
    void prepara() {
        MondoDiProva.nuovo(41);
        RegistroMissioni.reimposta();
        RegistroMissioni.registrati();
    }

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    @Test
    void gliEventiPortanoICoseDelleMissioni() {
        Missione missione = RegistroMissioni.getMissionePrincipale();
        assertSame(missione, new InternoMissioneCompletata(missione).getMissione());
        NotificaAggiornamentoStatoMissione notifica = new NotificaAggiornamentoStatoMissione(null, "etichetta", "descrizione");
        assertNull(notifica.getMissione());
        assertEquals("etichetta", notifica.getEtichetta());
        assertEquals("descrizione", notifica.getDescrizione());
        List<Comando> comandi = new ArrayList<>(Arrays.asList(Comando.SI, Comando.NO));
        assertNotNull(new RichiestaSelezioneMissione(comandi));
    }

    @Test
    void laSfidaACartaForbiciSassoFiniscePerChiVinceLeMani() {
        boolean haVintoIlGiocatore = false;
        boolean haVintoLAvversario = false;
        for (int seme = 1; seme <= 40 && !(haVintoIlGiocatore && haVintoLAvversario); seme++) {
            Dado.impostaSeme(seme);
            PartitaCartaForbiciSasso partita = new PartitaCartaForbiciSasso();
            assertFalse(partita.isFinita());
            assertFalse(partita.haVintoIlGiocatore());
            while (!partita.isFinita()) {
                PartitaCartaForbiciSasso.Mano mano = partita.gioca(MossaCartaForbiciSasso.SASSO);
                assertEquals(MossaCartaForbiciSasso.SASSO, mano.getGiocatore());
                assertNotNull(mano.getAvversario());
            }
            assertThrows(IllegalStateException.class, () -> partita.gioca(MossaCartaForbiciSasso.CARTA));
            if (partita.haVintoIlGiocatore()) {
                haVintoIlGiocatore = true;
                assertTrue(partita.getVittorieDelGiocatore() > partita.getVittorieDellAvversario());
            } else {
                haVintoLAvversario = true;
                assertTrue(partita.getVittorieDellAvversario() > partita.getVittorieDelGiocatore());
            }
        }
        assertTrue(haVintoIlGiocatore && haVintoLAvversario, "in quaranta sfide vincono entrambi");
        assertTrue(PartitaCartaForbiciSasso.puoGiocare(TipoPersonaggio.GOBLIN));
        assertFalse(PartitaCartaForbiciSasso.puoGiocare(TipoPersonaggio.OMBRAFIAMMA));
    }

    @Test
    void ilSacerdoteSiProponeSoloAChiHaPostoENonHaGiaAccolto() {
        GruppoGiocatore gruppo = GruppoGiocatore.getIstanza();
        List<String> detti = new ArrayList<>();
        BusEventi.iscriviti(NotificaTestoParagrafo.class, e -> detti.add(e.getMessaggio()));
        while (gruppo.getNumeroPersonaggiPermanenti() < CostantiPersonaggi.MAX_PERSONAGGI_GRUPPO_GIOCATORE) {
            Personaggio p = new Guerriero("Comparsa" + gruppo.getNumeroPersonaggiPermanenti(), 1);
            p.getModelloDati().setPng(false);
            gruppo.aggiungiPersonaggio(p);
        }
        assertFalse(OffertaDelSacerdote.puoProporsi());
        OffertaDelSacerdote.avvisaSeIlGruppoEPieno("il sacerdote del sole");
        assertEquals(1, detti.size());
        assertTrue(detti.get(0).contains("Siete già in"), detti.get(0));
        gruppo.setSacerdoteAccolto(true);
        assertFalse(OffertaDelSacerdote.puoProporsi());
        OffertaDelSacerdote.avvisaSeIlGruppoEPieno("il sacerdote del sole");
        assertEquals(1, detti.size(), "chi è già entrato non ha niente da sentirsi dire");
    }

    @Test
    void gliIntermezziDelleMissioniSiChiedonoInOgniMomento() {
        for (MomentoIntermezzo momento : MomentoIntermezzo.values()) {
            assertNull(IntermezziDelleMissioni.ISTANZA.getProssimo(momento), momento.name());
            // Una missione appena nata può avere un primo passo che parte con un intermezzo: si risponde, in ogni momento
            IntermezziDelleMissioni.ISTANZA.haUnIntermezzoInArrivo(momento);
        }
        for (TipoIntermezzo tipo : TipoIntermezzo.values()) {
            assertFalse(IntermezziDelleMissioni.ISTANZA.blocca(tipo), tipo.name());
        }
    }

    @Test
    void ogniMissioneNuovaDiceNomeEDescrizione() {
        for (Missione m : RegistroMissioni.getTutteLeMissioni()) {
            assertNotNull(m.getNome(), m.getClass().getSimpleName());
            assertNotNull(m.getDescrizione(), m.getClass().getSimpleName());
        }
    }
}
