package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.modellodati.NotiziaMD;
import com.threeamigos.foresta.notizie.Notizie;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Uscendo da una città il gruppo si lascia dietro una notizia, scelta fra quelle di NOTIZIE_CITTA (locande.txt): finisce nel notiziario e nel
 * testo del gioco, adattata ai personaggi del gruppo.
 */
class ScenarioNotizieDellaCittaTest {

    @Test
    void uscendoDaUnaCittaUnaNotiziaDelleCittaVaNelNotiziario() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(95)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            int prima = Notizie.getUltimeNotizie().size();

            partita.comando(Comando.ESCI_DA_CITTA);

            List<NotiziaMD> notizie = Notizie.getUltimeNotizie();
            assertEquals(prima + 1, notizie.size());
            NotiziaMD notizia = notizie.get(0);
            assertTrue(notizia.getId().startsWith("CG"), notizia.getId());
            assertFalse(notizia.getCorpo().contains("EROE"), "i nomi si adattano al gruppo: " + notizia.getCorpo());
            assertTrue(partita.testi().contains(notizia.getCorpo()), "e si legge anche nel testo del gioco");
        }
    }

    @Test
    void sePassandoDallaLocandaLaLocandaHaRaccontatoUnaNotiziaLaCittaNonNeAggiungeUnAltra() {
        boolean locandaConNotizia = false;
        for (long seme = 1; seme <= 12; seme++) {
            try (PartitaDiTest partita = PartitaDiTest.nuova(seme)) {
                partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
                Cassa.addMonete(500);
                int prima = Notizie.getUltimeNotizie().size();

                partita.comando(Comando.LOCANDA);
                for (int passo = 0; passo < 12; passo++) {
                    Collection<Comando> comandi = partita.comandiDisponibili();
                    if (comandi.contains(Comando.ESCI_DA_CITTA) || comandi.isEmpty()) {
                        break;
                    }
                    if (comandi.contains(Comando.SI) && comandi.contains(Comando.NO)) {
                        partita.comando(Comando.NO);
                    } else {
                        partita.comando(comandi.iterator().next());
                    }
                }
                int dopoLaLocanda = Notizie.getUltimeNotizie().size();
                partita.comando(Comando.ESCI_DA_CITTA);
                int dopoLaCitta = Notizie.getUltimeNotizie().size();

                // Una notizia per visita: o quella della locanda, o, se la locanda non ne ha avute, quella della città
                assertEquals(prima + 1, dopoLaCitta, "seme " + seme);
                if (dopoLaLocanda > prima) {
                    locandaConNotizia = true;
                    assertEquals(dopoLaLocanda, dopoLaCitta, "seme " + seme + ": la città non aggiunge la sua");
                }
            }
        }
        assertTrue(locandaConNotizia, "nessun seme in cui la locanda racconti una notizia: il test non prova niente");
    }
}
