package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.notifiche.NotificaAggiornamentoStatoMissione;
import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.intermezzi.BattutaProgrammata;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.LaLeggendaDiMalgaard;
import com.threeamigos.foresta.missioni.LaLeggendaDiNyena;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.RecuperaUnArtefattoLeggendario;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.oggetti.Artefatto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le missioni degli artefatti leggendari: a Nyena e a Malgaard l'armaiolo racconta una leggenda, sorge un tempio
 * che custodisce il leggendario, e raccoglierlo completa la missione.
 */
class ScenarioArtefattiLeggendariTest {

    @Test
    void aNyenaLArmaioloRaccontaLaLeggendaPoiSorgeIlTempioConIlLeggendario() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(41)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> {
                partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA);
                partita.eventi().ascolta(NotificaPaginaIntermezzo.class, NotificaAggiornamentoStatoMissione.class);
            });
            RecuperaUnArtefattoLeggendario missione = trova(LaLeggendaDiNyena.class);
            ArtefattoLeggendario leggendario = missione.getLeggendario();
            assertNotNull(leggendario);
            assertTrue(missione.isAttiva());
            assertEquals("Recupera " + leggendario.getNomeBreve(), missione.getNome());

            // Prima la pagina con la leggenda, poi l'avviso della nuova missione
            List<Object> eventi = partita.eventi().inOrdine(NotificaPaginaIntermezzo.class, NotificaAggiornamentoStatoMissione.class);
            int pagina = -1;
            int avviso = -1;
            for (int i = 0; i < eventi.size(); i++) {
                Object evento = eventi.get(i);
                if (pagina < 0 && evento instanceof NotificaPaginaIntermezzo
                        && battute((NotificaPaginaIntermezzo) evento).contains(leggendario.getLeggenda().get(0))) {
                    pagina = i;
                }
                if (evento instanceof NotificaAggiornamentoStatoMissione
                        && missione.getNome().equals(((NotificaAggiornamentoStatoMissione) evento).getDescrizione())) {
                    avviso = i;
                }
            }
            assertTrue(pagina >= 0, "l'armaiolo racconta la leggenda");
            assertTrue(avviso > pagina, "l'avviso di nuova missione arriva dopo l'intermezzo");

            // Il tempio nuovo custodisce il leggendario, è della missione ed è segnato sulla mappa
            CoordinateMD tempio = RegistroMissioni.getLocazioneOccupata(missione);
            assertEquals(ClassiLocazione.TEMPIO, Foresta.getLocazione(tempio));
            Artefatto custodito = RegistroArtefatti.getArtefattoInLocazione(tempio);
            assertEquals(leggendario.costruisci().getNome(), custodito.getNome());
            assertEquals(TipoRaritaArtefatto.LEGGENDARIO, custodito.getRarita());
            assertTrue(RegistroArtefatti.isLocalizzazioneConosciuta(tempio));
            assertTrue(Foresta.isLocazioneConosciuta(tempio));
        }
    }

    @Test
    void leDueCittaPescanoLeggendariDiversiERaccoglierloCompletaLaMissione() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(42)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            RecuperaUnArtefattoLeggendario nyena = trova(LaLeggendaDiNyena.class);
            RecuperaUnArtefattoLeggendario malgaard = trova(LaLeggendaDiMalgaard.class);

            // Il gruppo arriva a Malgaard: anche lì l'armaiolo ha una leggenda, ma di un altro artefatto
            partita.gruppo().setCoordinate(Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_MALGAARD));
            malgaard.controllaPreLocazione();
            malgaard.controllaInLocazione();
            assertTrue(malgaard.isAttiva());
            assertNotNull(malgaard.getLeggendario());
            assertNotEquals(nyena.getLeggendario(), malgaard.getLeggendario());
            CoordinateMD tempio = RegistroMissioni.getLocazioneOccupata(malgaard);
            assertNotEquals(RegistroMissioni.getLocazioneOccupata(nyena), tempio);

            // Sconfitte le viverne, il gruppo raccoglie l'artefatto: a fine locazione la missione è completa
            partita.gruppo().setCoordinate(tempio);
            malgaard.controllaPostLocazione();
            assertFalse(malgaard.isCompleta(), "l'artefatto è ancora nel tempio");
            RegistroArtefatti.rimuoviArtefattoInLocazione(tempio);
            malgaard.controllaPostLocazione();
            assertTrue(malgaard.isCompleta());
            assertEquals(Optional.of("Qui le viverne custodivano " + malgaard.getLeggendario().getNomeBreve() + "."),
                    RegistroMissioni.getRicordo(tempio));
        }
    }

    private static String battute(NotificaPaginaIntermezzo evento) {
        return evento.getPagina().getBattuteProgrammate().stream().map(BattutaProgrammata::getBattuta)
                .map(b -> b.getTesto()).collect(Collectors.joining(" "));
    }

    private static RecuperaUnArtefattoLeggendario trova(Class<? extends Missione> tipo) {
        return (RecuperaUnArtefattoLeggendario) RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).findFirst()
                .orElseThrow(() -> new AssertionError("missione " + tipo.getSimpleName() + " non trovata"));
    }
}
