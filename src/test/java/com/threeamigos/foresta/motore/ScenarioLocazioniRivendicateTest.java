package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.ClasseMissione;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.SconfiggiIlLich;
import com.threeamigos.foresta.missioni.SconfiggiIlMinotauroGigante;
import com.threeamigos.foresta.missioni.SconfiggiLIdra;
import com.threeamigos.foresta.missioni.SconfiggiLaStrega;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le locazioni rivendicate dalle missioni (gestione_missioni.md, §6-7): i castelli che le missioni si procurano a
 * inizio partita, la ricerca a quadrati concentrici, i claim che non si cancellano mai.
 */
class ScenarioLocazioniRivendicateTest {

    /**
     * Una missione che al primo controllo si procura un tempio.
     */
    static class MissioneDelTempio extends MissioneAPassi {

        MissioneDelTempio() {
            super(ClasseMissione.MISSIONE_DI_PROVA);
        }

        @Override
        protected String passoIniziale() {
            return "CERCA";
        }

        @Override
        protected Passo costruisciPasso(String id) {
            if ("CERCA".equals(id)) {
                return cercaLocazione(MomentoControllo.PRE_LOCAZIONE, ClassiLocazione.TEMPIO).esegui(this::attivaMissione).poi("ARRIVATI");
            }
            return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> false);
        }
    }

    private static PartitaDiTest nuovaPartita(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuova(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
        return partita;
    }

    private static MissioneDelTempio aggiungi(MissioneDelTempio missione) {
        RegistroMissioni.getMissionePrincipale().aggiungiMissione(missione);
        return missione;
    }

    @Test
    void iQuattroCastelliLiRivendicanoLeLoroMissioniAInizioPartita() {
        try (PartitaDiTest partita = nuovaPartita(31)) {
            Object[][] castelli = {
                    {SconfiggiLaStrega.class, ClassiLocazione.CASTELLO_STREGA},
                    {SconfiggiIlLich.class, ClassiLocazione.CASTELLO_LICH},
                    {SconfiggiIlMinotauroGigante.class, ClassiLocazione.CASTELLO_MINOTAURO},
                    {SconfiggiLIdra.class, ClassiLocazione.CASTELLO_IDRA},
            };
            for (Object[] castello : castelli) {
                Missione missione = RegistroMissioni.getMissionePrincipale().getMissioniSecondarie().stream()
                        .filter(((Class<?>) castello[0])::isInstance).findFirst().orElseThrow(AssertionError::new);
                CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica((ClassiLocazione) castello[1]);
                assertNotNull(coordinate, castello[1] + " deve esistere");
                assertTrue(missione.isAttiva());
                assertEquals(coordinate, RegistroMissioni.getLocazioneOccupata(missione));
                assertSame(missione, RegistroMissioni.getMissioneCheHaOccupato(coordinate).orElse(null));
            }
            assertNull(Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CASTELLO_DRAGO), "il Drago si mostra solo dopo");
        }
    }

    @Test
    void ilBordoDiUnQuadratoHaOttoCellePerRaggioTutteAQuellaDistanza() {
        assertEquals(1, RegistroMissioni.bordo(5, 5, 0).size());
        for (int raggio = 1; raggio <= 4; raggio++) {
            List<CoordinateMD> bordo = RegistroMissioni.bordo(5, 5, raggio);
            assertEquals(8 * raggio, bordo.size());
            assertEquals(bordo.size(), new HashSet<>(bordo).size(), "nessuna cella ripetuta");
            for (CoordinateMD cella : bordo) {
                assertEquals(raggio, Math.max(Math.abs(cella.getX() - 5), Math.abs(cella.getY() - 5)));
            }
        }
    }

    @Test
    void ilPassoCercaLocazioneSiProcuraUnTempioLiberoENonQuelloDelGruppo() {
        try (PartitaDiTest partita = nuovaPartita(32)) {
            MissioneDelTempio missione = aggiungi(new MissioneDelTempio());
            missione.controllaPreLocazione();
            assertEquals("ARRIVATI", missione.getPassoCorrente());
            CoordinateMD tempio = RegistroMissioni.getLocazioneOccupata(missione);
            assertEquals(ClassiLocazione.TEMPIO, Foresta.getLocazione(tempio));
            assertNotEquals(partita.gruppo().getCoordinate(), tempio);
        }
    }

    @Test
    void unaLocazioneDiUnaMissioneInCorsoNonSiPrendeQuellaDiUnaMissioneFinitaSiERestaLUltimoProprietario() {
        try (PartitaDiTest partita = nuovaPartita(33)) {
            MissioneDelTempio prima = aggiungi(new MissioneDelTempio());
            MissioneDelTempio seconda = aggiungi(new MissioneDelTempio());
            List<CoordinateMD> templi = coordinate(ClassiLocazione.TEMPIO);
            // La prima rivendica tutti i templi tranne l'ultimo, che diventa la casella del gruppo
            CoordinateMD ultimo = templi.remove(templi.size() - 1);
            for (CoordinateMD tempio : templi) {
                RegistroMissioni.occupaLocazione(tempio, prima);
            }
            partita.gruppo().setCoordinate(ultimo);
            assertEquals(Optional.empty(), RegistroMissioni.cerca(ClassiLocazione.TEMPIO, seconda),
                    "i templi sono tutti della prima missione, in corso, o del gruppo");

            prima.completaMissione();
            Optional<CoordinateMD> trovato = RegistroMissioni.cerca(ClassiLocazione.TEMPIO, seconda);
            assertTrue(trovato.isPresent(), "finita la prima missione, i suoi templi tornano disponibili");
            assertSame(seconda, RegistroMissioni.getMissioneCheHaOccupato(trovato.get()).orElse(null));
            // Gli altri ricordano ancora chi li aveva
            templi.remove(trovato.get());
            assertSame(prima, RegistroMissioni.getMissioneCheHaOccupato(templi.get(0)).orElse(null));
        }
    }

    @Test
    void dopoUnCaricamentoIClaimSiRicostruisconoEVinceLUltimo() {
        try (PartitaDiTest partita = nuovaPartita(34)) {
            MissioneDelTempio prima = aggiungi(new MissioneDelTempio());
            MissioneDelTempio seconda = aggiungi(new MissioneDelTempio());
            CoordinateMD tempio = coordinate(ClassiLocazione.TEMPIO).get(0);
            RegistroMissioni.occupaLocazione(tempio, prima);
            RegistroMissioni.occupaLocazione(tempio, seconda);
            CoordinateMD strega = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CASTELLO_STREGA);

            GestoreSalvataggi.salva(Comando.NUMERO_3);
            assertTrue(GestoreSalvataggi.leggi(Comando.NUMERO_3));

            assertEquals(seconda.getId(), RegistroMissioni.getMissioneCheHaOccupato(tempio).map(Missione::getId).orElse(null));
            assertTrue(RegistroMissioni.getMissioneCheHaOccupato(strega).orElse(null) instanceof SconfiggiLaStrega);
            // Un claim fatto dopo il caricamento vince su quelli di prima
            Missione primaRiletta = RegistroMissioni.getTutteLeMissioni().stream()
                    .filter(m -> m.getId().equals(prima.getId())).findFirst().orElseThrow(AssertionError::new);
            RegistroMissioni.occupaLocazione(tempio, primaRiletta);
            GestoreSalvataggi.salva(Comando.NUMERO_3);
            assertTrue(GestoreSalvataggi.leggi(Comando.NUMERO_3));
            assertEquals(prima.getId(), RegistroMissioni.getMissioneCheHaOccupato(tempio).map(Missione::getId).orElse(null));
        }
    }

    private static List<CoordinateMD> coordinate(ClassiLocazione classe) {
        List<CoordinateMD> trovate = new ArrayList<>();
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == classe) {
                    trovate.add(new CoordinateMD(x, y));
                }
            }
        }
        return trovate;
    }
}
