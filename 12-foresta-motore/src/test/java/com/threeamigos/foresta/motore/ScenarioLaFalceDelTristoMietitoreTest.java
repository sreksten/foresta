package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.eventi.notifiche.NotificaPaginaIntermezzo;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.leggende.LaFalceDelTristoMietitore;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La falce del Tristo Mietitore: un personaggio spaventato in città dice di aver visto la Morte nelle rovine, che si segnano sulla mappa; lì
 * c'è un necromante con quattro scheletri (due, lui, due), che ne evoca altri finché è vivo, e la sua falce è il premio.
 */
class ScenarioLaFalceDelTristoMietitoreTest {

    private static LaFalceDelTristoMietitore la() {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(LaFalceDelTristoMietitore.class::isInstance)
                .map(LaFalceDelTristoMietitore.class::cast).findFirst().orElseThrow(AssertionError::new);
    }

    private static void inCitta(PartitaDiTest partita) {
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> {
            partita.rimandaLeMissioniCheSiOffrono();
            partita.rendiDisponibile(la());
            partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA);
            partita.eventi().ascolta(NotificaPaginaIntermezzo.class);
        });
    }

    @Test
    void inCittaUnPersonaggioSpaventatoRaccontaLaMorteEDaLeRovineSegnateSullaMappa() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(301)) {
            inCitta(partita);
            LaFalceDelTristoMietitore missione = la();
            assertTrue(missione.isAttiva());
            assertEquals("La falce del Tristo Mietitore", missione.getNome());

            String pagine = partita.eventi().tutti(NotificaPaginaIntermezzo.class).toString();
            assertFalse(partita.eventi().tutti(NotificaPaginaIntermezzo.class).isEmpty());
            CoordinateMD rovine = missione.getCovo();
            assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(rovine));
            assertTrue(Foresta.isLocazioneConosciuta(rovine), "le rovine sono sulla mappa");
            assertTrue(CostruttoreDellaForesta.getCoordinateDaSegnalare().contains(rovine), pagine);
        }
    }

    @Test
    void nelleRovineCeIlNecromanteAlCentroConDueScheletriPerParte() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(302)) {
            inCitta(partita);
            List<Personaggio> incontro = la().getIncontroInLocazione(la().getCovo()).orElseThrow(AssertionError::new);
            assertEquals(Stream5.CLASSI, incontro.stream().map(Personaggio::getClasse).collect(Collectors.toList()));
            assertEquals(la().getNomeDelNecromante(), incontro.get(2).getNome());
        }
    }

    @Test
    void ognuniDueScheletriCadutiIlNecromanteVivoNeEvocaUnAltroEAlTermineLaFalceEDelGruppo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(303)) {
            inCitta(partita);
            LaFalceDelTristoMietitore missione = la();
            CoordinateMD rovine = missione.getCovo();
            // Il gruppo regge: lo scontro non si decide sui dadi
            partita.gruppo().getCapo().getModelloDati().set(TipoAttributo.SALUTE, 1_000_000);
            partita.comando(Comando.ESCI_DA_CITTA);
            Foresta.setPosizioneDelGruppo(new CoordinateMD(rovine.getX(), rovine.getY() + 1));
            partita.assertStato(Stato.SCELTA_DIREZIONE);
            partita.comando(Comando.NORD).comando(Comando.NUMERO_1);
            assertEquals(rovine, Foresta.getPosizioneDelGruppo());
            List<Personaggio> avversari = GruppoAvversario.getIstanza().getPersonaggi();
            assertEquals(5, avversari.size());
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("quattro Scheletri e un Necromante")), String.valueOf(partita.testi()));
            Personaggio necromante = avversari.stream().filter(p -> p.getClasse() == TipoPersonaggio.NECROMANTE).findFirst()
                    .orElseThrow(AssertionError::new);
            List<Personaggio> scheletri = avversari.stream().filter(p -> p.getClasse() == TipoPersonaggio.SCHELETRO).collect(Collectors.toList());

            // Cadono due scheletri, il necromante è vivo: ne evoca un altro, che entra nel gruppo avversario
            scheletri.get(0).muore("è caduto");
            scheletri.get(1).muore("è caduto");
            partita.comando(Comando.COMBATTIMENTO);
            String evocazione = necromante.getNome() + " evoca un altro scheletro";
            for (int giri = 0; giri < 20 && partita.stato() == Stato.IN_COMBATTIMENTO && partita.testi().stream().noneMatch(t -> t.contains(evocazione)); giri++) {
                partita.scatta();
            }
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains(evocazione)),
                    String.valueOf(partita.testi()));
            assertEquals(6, GruppoAvversario.getIstanza().getPersonaggi().size());
            assertEquals(3, GruppoAvversario.getIstanza().getPersonaggiVivi().stream().filter(p -> p.getClasse() == TipoPersonaggio.SCHELETRO).count());

            // Cade il necromante: gli scheletri che restano crollano all'istante, senza che nessuno li colpisca, e non se ne evoca più
            assertTrue(GruppoAvversario.getIstanza().getPersonaggiVivi().stream().filter(p -> p.getClasse() == TipoPersonaggio.SCHELETRO).count() >= 3);
            necromante.muore("è caduto");
            if (partita.stato() != Stato.IN_COMBATTIMENTO) {
                partita.comando(Comando.COMBATTIMENTO);
            }
            for (int giri = 0; partita.stato() == Stato.IN_COMBATTIMENTO && giri < 50; giri++) {
                partita.scatta();
            }
            assertEquals(1, partita.testi().stream().filter(t -> t.contains("evoca un altro scheletro")).count(), "senza necromante non si evoca più");
            assertTrue(partita.testi().stream().anyMatch(t -> t.contains("gli scheletri che teneva in piedi crollano in un mucchio d'ossa")),
                    String.valueOf(partita.testi()));
            assertFalse(partita.testi().stream().anyMatch(t -> t.contains("Aprendo il cofano")), "nelle rovine del necromante non c'è nessun oggetto");
            assertTrue(missione.isCompleta(), partita.stato() + " " + missione.getPassoCorrente() + " " + partita.testi());
            assertTrue(partita.testi().stream().noneMatch(t -> t.contains("_")), "un testo con un trattino basso: " + partita.testi().stream().filter(t -> t.contains("_")).collect(Collectors.toList()));
            String falce = Leggendari.con(LaFalceDelTristoMietitore.CHIAVE_DELLA_FALCE).getNome();
            assertTrue(partita.gruppo().getInventario().stream().anyMatch(a -> a.getTipo() == TipoArtefatto.FALCE && falce.equals(a.getNome())));
        }
    }

    @Test
    void laFalceNonSiGeneraMaiACaso() {
        assertFalse(TipoArtefatto.FALCE.isGenerabileACaso());
        try (PartitaDiTest partita = PartitaDiTest.nuova(304)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            for (int livello = 1; livello <= 8; livello++) {
                for (int i = 0; i < 400; i++) {
                    assertNotEquals(TipoArtefatto.FALCE, com.threeamigos.foresta.artefatti.GeneratoreArtefatti.istanza().generaArtefattoCasuale(livello).getTipo());
                }
            }
        }
    }

    private static final class Stream5 {
        static final List<TipoPersonaggio> CLASSI = java.util.Arrays.asList(TipoPersonaggio.SCHELETRO, TipoPersonaggio.SCHELETRO,
                TipoPersonaggio.NECROMANTE, TipoPersonaggio.SCHELETRO, TipoPersonaggio.SCHELETRO);
    }
}
