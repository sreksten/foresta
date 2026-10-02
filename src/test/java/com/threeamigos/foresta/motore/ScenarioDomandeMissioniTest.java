package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locazione;
import com.threeamigos.foresta.missioni.ClasseMissione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Una missione a passi pone una domanda al giocatore nel mezzo di una partita vera (gestione_missioni.md, §4).
 */
class ScenarioDomandeMissioniTest {

    /**
     * Entrando in una locazione chiede quale strada prendere fra tre, poi segue il ramo scelto e si completa.
     */
    static class MissioneDelBivio extends MissioneAPassi {

        final List<String> eseguiti = new ArrayList<>();

        MissioneDelBivio() {
            super(ClasseMissione.MISSIONE_DI_PROVA);
        }

        @Override
        protected String passoIniziale() {
            return "BIVIO";
        }

        @Override
        protected Passo costruisciPasso(String id) {
            if ("BIVIO".equals(id)) {
                return Passo.quando(MomentoControllo.IN_LOCAZIONE, () -> true)
                        .chiediScelta("Davanti a voi la strada si divide in tre. Quale prendete?",
                                Arrays.asList("Il sentiero del lupo", "La strada maestra", "Il guado"))
                        .esegui(this::attivaMissione)
                        .poi(() -> "STRADA_" + getRisposta("BIVIO"));
            }
            return Passo.quando(MomentoControllo.IN_LOCAZIONE, () -> true).esegui(() -> eseguiti.add(id)).poi(Passo.FINE);
        }
    }

    @Test
    void laDomandaFermaIlGiocoLaRispostaScegliIlRamoELaLocazioneNonSiRifa() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(21)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            MissioneDelBivio missione = new MissioneDelBivio();
            RegistroMissioni.getMissionePrincipale().aggiungiMissione(missione);

            partita.comando(Comando.ESCI_DA_CITTA);
            partita.assertStato(Stato.SCELTA_DIREZIONE);
            for (Comando direzione : new Comando[]{Comando.NORD, Comando.EST, Comando.SUD, Comando.OVEST}) {
                if (partita.comandiDisponibili().contains(direzione)) {
                    partita.comando(direzione).comando(Comando.NUMERO_1);
                    break;
                }
            }

            // Nella locazione nuova, dopo che è stata costruita e descritta, la missione chiede
            partita.assertStato(Stato.ATTESA_RISPOSTA_MISSIONE);
            assertEquals(Arrays.asList(Comando.NUMERO_1, Comando.NUMERO_2, Comando.NUMERO_3), new ArrayList<>(partita.comandiDisponibili()));
            Locazione locazione = partita.gruppo().getLocazioneCorrente();

            partita.comando(Comando.NUMERO_2);
            assertNotEquals(Stato.ATTESA_RISPOSTA_MISSIONE, partita.stato());
            assertEquals("2", missione.getRisposta("BIVIO"));
            assertEquals(Arrays.asList("STRADA_2"), missione.eseguiti);
            assertTrue(missione.isCompleta());
            assertSame(locazione, partita.gruppo().getLocazioneCorrente(), "la locazione non si costruisce di nuovo");
        }
    }
}
