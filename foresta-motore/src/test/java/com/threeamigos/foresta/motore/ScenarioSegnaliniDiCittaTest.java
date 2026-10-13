package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.missioni.CacciaAiGoblin;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.NonSparateSulPianista;
import com.threeamigos.foresta.missioni.RecuperaIlMedaglione;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dove una missione manda il gruppo (la città in cui riportare qualcuno o tornare a riscuotere) la casella lampeggia
 * sulla mappa, con il nome della missione, finché il passo che la segna è quello corrente: anche se più missioni
 * mandano nella stessa città, e anche dopo un salvataggio.
 */
class ScenarioSegnaliniDiCittaTest {

    private static <T extends Missione> T trova(Class<T> tipo) {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(tipo::isInstance).map(tipo::cast).findFirst()
                .orElseThrow(AssertionError::new);
    }

    private static CoordinateMD unaLocanda() {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == TipoLocazione.LOCANDA) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("nessuna locanda nel bosco");
    }

    /**
     * Il bardo si affida al gruppo alla terza visita della locanda: la missione passa al viaggio verso la città.
     */
    private static NonSparateSulPianista conIlBardoInViaggio(PartitaDiTest partita) {
        NonSparateSulPianista pianista = trova(NonSparateSulPianista.class);
        CoordinateMD locanda = unaLocanda();
        partita.gruppo().setCoordinate(locanda);
        Foresta.getLocazioneMD(locanda).aggiungiProprieta(Locanda.LOCANDA_VISITE, "2");
        pianista.controllaPreLocazione();
        pianista.segnaIntermezzoPassoMostrato("INCARICO");
        pianista.controllaInLocazione();
        assertEquals("VIAGGIO", pianista.getPassoCorrente());
        return pianista;
    }

    @Test
    void laCittaDelBardoLampeggiaFinoAlArrivo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(151)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            NonSparateSulPianista pianista = conIlBardoInViaggio(partita);
            CoordinateMD casa = Foresta.getCoordinateLocazioneUnica(pianista.getCitta());

            assertTrue(Foresta.getCoordinateDaSegnalare().contains(casa), "la città di casa lampeggia");
            assertEquals(java.util.Collections.singletonList(pianista.getNome()), Foresta.getNomiMissioniDaMostrare(casa));
            assertNull(RegistroMissioni.getLocazioneOccupata(pianista), "non si rivendica: il contenuto della città resta suo");

            // Arrivati, il passo si conclude e il segnalino sparisce
            partita.gruppo().setCoordinate(casa);
            pianista.controllaPreLocazione();
            assertEquals("ARRIVO", pianista.getPassoCorrente());
            assertFalse(Foresta.getCoordinateDaSegnalare().contains(casa));
            assertTrue(Foresta.getNomiMissioniDaMostrare(casa).isEmpty());
        }
    }

    @Test
    void ilSegnalinoSopravvivealSalvataggio() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(151)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            NonSparateSulPianista pianista = conIlBardoInViaggio(partita);
            CoordinateMD casa = Foresta.getCoordinateLocazioneUnica(pianista.getCitta());
            String nome = pianista.getNome();

            assertTrue(partita.salva(Comando.NUMERO_1));
            assertTrue(partita.leggi(Comando.NUMERO_1));

            assertTrue(Foresta.getCoordinateDaSegnalare().contains(casa));
            assertEquals(java.util.Collections.singletonList(nome), Foresta.getNomiMissioniDaMostrare(casa));
        }
    }

    @Test
    void piuMissioniNellaStessaCittaNeSegnanoUnaSolaVoltaEPerOgniNomeUnaRiga() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(151)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            NonSparateSulPianista pianista = conIlBardoInViaggio(partita);
            CoordinateMD casa = Foresta.getCoordinateLocazioneUnica(pianista.getCitta());
            MissioneAPassi altra = trova(CacciaAiGoblin.class);
            altra.attivaMissione();
            RegistroMissioni.segnalaLocazione(altra, casa);

            assertEquals(1, Foresta.getCoordinateDaSegnalare().stream().filter(casa::equals).count(), "una casella sola");
            List<String> nomi = Foresta.getNomiMissioniDaMostrare(casa);
            assertEquals(2, nomi.size(), String.valueOf(nomi));
            assertTrue(nomi.contains(pianista.getNome()) && nomi.contains(altra.getNome()));

            // Una delle due finisce: l'altra continua a segnare la città
            RegistroMissioni.togliSegnalino(altra);
            assertTrue(Foresta.getCoordinateDaSegnalare().contains(casa));
            assertEquals(java.util.Collections.singletonList(pianista.getNome()), Foresta.getNomiMissioniDaMostrare(casa));
        }
    }

    @Test
    void tornandoARiscuotereLaCittaDellIncaricoLampeggia() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(22)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
            MissioneAPassi medaglione = trova(RecuperaIlMedaglione.class);
            CoordinateMD fleena = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_FLEENA);

            // Sconfitti i ladri si torna in città, ma il gruppo è altrove: Fleena lampeggia
            medaglione.aggiungiProprieta("PASSO_CORRENTE", "RITORNO");
            partita.gruppo().setCoordinate(RegistroMissioni.getLocazioneOccupata(medaglione));
            medaglione.controllaPreLocazione();
            assertTrue(Foresta.getCoordinateDaSegnalare().contains(fleena), "la città del mandante lampeggia");
            assertTrue(Foresta.getNomiMissioniDaMostrare(fleena).contains(medaglione.getNome()));

            // Tornati: ringraziamento, ricompensa, e il segnalino sparisce
            partita.gruppo().setCoordinate(fleena);
            medaglione.controllaPreLocazione();
            medaglione.segnaIntermezzoPassoMostrato("RITORNO");
            medaglione.controllaInLocazione();
            assertTrue(medaglione.isCompleta());
            assertFalse(Foresta.getNomiMissioniDaMostrare(fleena).contains(medaglione.getNome()));
        }
    }
}
