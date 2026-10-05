package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.missioni.ClasseMissione;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.Passo.MomentoControllo;
import com.threeamigos.foresta.missioni.Passo;
import com.threeamigos.foresta.missioni.SconfiggiIlDrago;
import com.threeamigos.foresta.missioni.SconfiggiIlLich;
import com.threeamigos.foresta.missioni.SconfiggiIlMinotauroGigante;
import com.threeamigos.foresta.missioni.SconfiggiLIdra;
import com.threeamigos.foresta.missioni.SconfiggiLaStrega;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
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
                return cercaLocazione(MomentoControllo.PRE_LOCAZIONE, TipoLocazione.TEMPIO).esegui(this::attivaMissione).poi("ARRIVATI");
            }
            return Passo.quando(MomentoControllo.PRE_LOCAZIONE, () -> false);
        }
    }

    private static PartitaDiTest nuovaPartita(long seme) {
        PartitaDiTest partita = PartitaDiTest.nuova(seme);
        partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
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
                    {SconfiggiLaStrega.class, TipoLocazione.CASTELLO_STREGA},
                    {SconfiggiIlLich.class, TipoLocazione.CASTELLO_LICH},
                    {SconfiggiIlMinotauroGigante.class, TipoLocazione.CASTELLO_MINOTAURO},
                    {SconfiggiLIdra.class, TipoLocazione.CASTELLO_IDRA},
            };
            for (Object[] castello : castelli) {
                Missione missione = RegistroMissioni.getMissionePrincipale().getMissioniSecondarie().stream()
                        .filter(((Class<?>) castello[0])::isInstance).findFirst().orElseThrow(AssertionError::new);
                CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica((TipoLocazione) castello[1]);
                assertNotNull(coordinate, castello[1] + " deve esistere");
                assertTrue(missione.isAttiva());
                assertEquals(coordinate, RegistroMissioni.getLocazioneOccupata(missione));
                assertSame(missione, RegistroMissioni.getMissioneCheHaOccupato(coordinate).orElse(null));
            }
            assertNull(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CASTELLO_DRAGO), "il Drago si mostra solo dopo");
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
            assertEquals(TipoLocazione.TEMPIO, Foresta.getLocazione(tempio));
            assertNotEquals(partita.gruppo().getCoordinate(), tempio);
        }
    }

    @Test
    void seNonCeUnTempioLiberoSeNeCostruisceUnoSuUnBoscoGiaVisitato() {
        try (PartitaDiTest partita = nuovaPartita(34)) {
            MissioneDelTempio prima = aggiungi(new MissioneDelTempio());
            MissioneDelTempio seconda = aggiungi(new MissioneDelTempio());
            for (CoordinateMD tempio : coordinate(TipoLocazione.TEMPIO)) {
                RegistroMissioni.occupaLocazione(tempio, prima);
            }
            assertEquals(Optional.empty(), RegistroMissioni.cerca(TipoLocazione.TEMPIO, seconda));

            // Un solo bosco visitato, e niente paludi visitate: il tempio nuovo sorge lì
            CoordinateMD visitato = coordinate(TipoLocazione.BOSCO).stream()
                    .filter(c -> !c.equals(partita.gruppo().getCoordinate()) && RegistroArtefatti.getArtefattoInLocazione(c) == null)
                    .findFirst().orElseThrow(AssertionError::new);
            for (TipoLocazione classe : new TipoLocazione[]{TipoLocazione.BOSCO, TipoLocazione.PALUDE}) {
                coordinate(classe).forEach(c -> Foresta.setLocazioneVisitata(c, false));
            }
            Foresta.setLocazioneVisitata(visitato, true);

            Optional<CoordinateMD> costruito = RegistroMissioni.cercaOCostruisci(TipoLocazione.TEMPIO, seconda);
            assertEquals(Optional.of(visitato), costruito);
            assertEquals(TipoLocazione.TEMPIO, Foresta.getLocazione(visitato));
            assertFalse(Foresta.isLocazioneVisitata(visitato), "il tempio nuovo è da visitare");
            assertSame(seconda, RegistroMissioni.getMissioneCheHaOccupato(visitato).orElse(null));
        }
    }

    @Test
    void unaLocazioneDiUnaMissioneInCorsoNonSiPrendeQuellaDiUnaMissioneFinitaSiERestaLUltimoProprietario() {
        try (PartitaDiTest partita = nuovaPartita(33)) {
            MissioneDelTempio prima = aggiungi(new MissioneDelTempio());
            MissioneDelTempio seconda = aggiungi(new MissioneDelTempio());
            List<CoordinateMD> templi = coordinate(TipoLocazione.TEMPIO);
            // La prima rivendica tutti i templi tranne l'ultimo, che diventa la casella del gruppo
            CoordinateMD ultimo = templi.remove(templi.size() - 1);
            for (CoordinateMD tempio : templi) {
                RegistroMissioni.occupaLocazione(tempio, prima);
            }
            partita.gruppo().setCoordinate(ultimo);
            assertEquals(Optional.empty(), RegistroMissioni.cerca(TipoLocazione.TEMPIO, seconda),
                    "i templi sono tutti della prima missione, in corso, o del gruppo");

            prima.completaMissione();
            Optional<CoordinateMD> trovato = RegistroMissioni.cerca(TipoLocazione.TEMPIO, seconda);
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
            CoordinateMD tempio = coordinate(TipoLocazione.TEMPIO).get(0);
            RegistroMissioni.occupaLocazione(tempio, prima);
            RegistroMissioni.occupaLocazione(tempio, seconda);
            CoordinateMD strega = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CASTELLO_STREGA);

            partita.salva(Comando.NUMERO_3);
            assertTrue(partita.leggi(Comando.NUMERO_3));

            assertEquals(seconda.getId(), RegistroMissioni.getMissioneCheHaOccupato(tempio).map(Missione::getId).orElse(null));
            assertTrue(RegistroMissioni.getMissioneCheHaOccupato(strega).orElse(null) instanceof SconfiggiLaStrega);
            // Un claim fatto dopo il caricamento vince su quelli di prima
            Missione primaRiletta = RegistroMissioni.getTutteLeMissioni().stream()
                    .filter(m -> m.getId().equals(prima.getId())).findFirst().orElseThrow(AssertionError::new);
            RegistroMissioni.occupaLocazione(tempio, primaRiletta);
            partita.salva(Comando.NUMERO_3);
            assertTrue(partita.leggi(Comando.NUMERO_3));
            assertEquals(prima.getId(), RegistroMissioni.getMissioneCheHaOccupato(tempio).map(Missione::getId).orElse(null));
        }
    }

    private static List<CoordinateMD> coordinate(TipoLocazione classe) {
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

    /**
     * Come se il gruppo avesse appena sconfitto chi stava nel castello: la locazione è completa e si azzera, come a
     * fine locazione, e la missione si completa.
     */
    private static CoordinateMD sconfiggi(PartitaDiTest partita, TipoLocazione castello, Missione missione) {
        CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(castello);
        Foresta.getLocazioneMD(coordinate).aggiungiProprieta(LocazioneMD.COMPLETA, LocazioneMD.AFFERMATIVO);
        Foresta.costruisciIstanza(coordinate).azzeraLocazione(partita.gruppo());
        missione.completaMissione();
        return coordinate;
    }

    private static Missione missione(Class<? extends Missione> tipo) {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).findFirst().orElseThrow(AssertionError::new);
    }

    @Test
    void ogniCastelloSconfittoDiventaRovineERicordaChiCiStava() {
        try (PartitaDiTest partita = nuovaPartita(35)) {
            Object[][] castelli = {
                    {SconfiggiLaStrega.class, TipoLocazione.CASTELLO_STREGA, "Qui sorgeva il castello della Strega."},
                    {SconfiggiIlLich.class, TipoLocazione.CASTELLO_LICH, "Qui sorgeva il castello del Lich."},
                    {SconfiggiIlMinotauroGigante.class, TipoLocazione.CASTELLO_MINOTAURO, "Qui sorgeva il castello del Minotauro Gigante."},
                    {SconfiggiLIdra.class, TipoLocazione.CASTELLO_IDRA, "Qui sorgeva il castello dell'Idra."},
            };
            for (Object[] castello : castelli) {
                Missione missione = missione((Class<? extends Missione>) castello[0]);
                CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica((TipoLocazione) castello[1]);
                assertEquals(Optional.empty(), RegistroMissioni.getRicordo(coordinate), "il castello c'è ancora");
                sconfiggi(partita, (TipoLocazione) castello[1], missione);
                assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(coordinate));
                assertNull(Foresta.getCoordinateLocazioneUnica((TipoLocazione) castello[1]));
                assertEquals(Optional.of(castello[2]), RegistroMissioni.getRicordo(coordinate));
            }

            // Sconfitti gli alleati, compare il castello del Drago, che a sua volta diventa rovine
            SconfiggiIlDrago drago = RegistroMissioni.getMissionePrincipale();
            drago.controllaPostLocazione();
            CoordinateMD castelloDrago = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CASTELLO_DRAGO);
            assertNotNull(castelloDrago);
            assertSame(drago, RegistroMissioni.getMissioneCheHaOccupato(castelloDrago).orElse(null));
            sconfiggi(partita, TipoLocazione.CASTELLO_DRAGO, drago);
            assertEquals(TipoLocazione.ROVINE, Foresta.getLocazione(castelloDrago));
            assertNull(Foresta.getCoordinateLocazioneUnica(TipoLocazione.CASTELLO_DRAGO));
            assertEquals(Optional.of("Qui sorgeva il castello del Drago."), RegistroMissioni.getRicordo(castelloDrago));
        }
    }

    @Test
    void entrandoFraLeRovineDiUnCastelloSiLeggeCheCosaCiSorgeva() {
        try (PartitaDiTest partita = nuovaPartita(36)) {
            CoordinateMD rovine = sconfiggi(partita, TipoLocazione.CASTELLO_STREGA, missione(SconfiggiLaStrega.class));
            // Il gruppo esce dalla città da una casella accanto alle rovine e ci entra con un passo
            boolean daSud = rovine.getY() + 1 < Foresta.getDimensioneY();
            partita.gruppo().setCoordinate(new CoordinateMD(rovine.getX(), rovine.getY() + (daSud ? 1 : -1)));
            partita.comando(Comando.ESCI_DA_CITTA);
            partita.assertStato(Stato.SCELTA_DIREZIONE);
            partita.eventi().ascolta(NotificaTestoFrase.class);
            partita.comando(daSud ? Comando.NORD : Comando.SUD).comando(Comando.NUMERO_1);

            assertEquals(rovine, partita.gruppo().getCoordinate());
            assertTrue(partita.testi().contains("Qui sorgeva il castello della Strega."), String.valueOf(partita.testi()));
        }
    }
}
