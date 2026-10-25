package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.MissioneAPassi;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.incarichi.CacciaAiGoblin;
import com.threeamigos.foresta.missioni.incarichi.IncaricoDiCombattimento;
import com.threeamigos.foresta.missioni.storie.NonSparateSulPianista;
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

    private static final String LADRI = "CHIAVE=LADRI_DI_PROVA;TIPO=RECUPERO;ASPETTO=QUALUNQUE;MANDANTE=il derubato;NEMICO=LADRO;"
            + "NUMERO=4;LUOGO=GROTTA;MONETE=20;TITOLO=Recupera il medaglione;RICHIESTA=Mi hanno rubato il medaglione.;"
            + "BATTUTA=Lo riavrai.;RISPOSTA=Grazie.;VITTORIA=I ladri sono a terra.;RINGRAZIAMENTO=Il mio medaglione!;"
            + "RICORDO=In questa grotta i ladri nascondevano il medaglione.";

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
        Foresta.setPosizioneDelGruppo(locanda);
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

            assertTrue(CostruttoreDellaForesta.getCoordinateDaSegnalare().contains(casa), "la città di casa lampeggia");
            assertEquals(java.util.Collections.singletonList(pianista.getNome()), CostruttoreDellaForesta.getNomiMissioniDaMostrare(casa));
            assertNull(RegistroMissioni.getLocazioneOccupata(pianista), "non si rivendica: il contenuto della città resta suo");

            // Arrivati, il passo si conclude e il segnalino sparisce
            Foresta.setPosizioneDelGruppo(casa);
            pianista.controllaPreLocazione();
            assertEquals("ARRIVO", pianista.getPassoCorrente());
            assertFalse(CostruttoreDellaForesta.getCoordinateDaSegnalare().contains(casa));
            assertTrue(CostruttoreDellaForesta.getNomiMissioniDaMostrare(casa).isEmpty());
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

            assertTrue(CostruttoreDellaForesta.getCoordinateDaSegnalare().contains(casa));
            assertEquals(java.util.Collections.singletonList(nome), CostruttoreDellaForesta.getNomiMissioniDaMostrare(casa));
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

            assertEquals(1, CostruttoreDellaForesta.getCoordinateDaSegnalare().stream().filter(casa::equals).count(), "una casella sola");
            List<String> nomi = CostruttoreDellaForesta.getNomiMissioniDaMostrare(casa);
            assertEquals(2, nomi.size(), String.valueOf(nomi));
            assertTrue(nomi.contains(pianista.getNome()) && nomi.contains(altra.getNome()));

            // Una delle due finisce: l'altra continua a segnare la città
            RegistroMissioni.togliSegnalino(altra);
            assertTrue(CostruttoreDellaForesta.getCoordinateDaSegnalare().contains(casa));
            assertEquals(java.util.Collections.singletonList(pianista.getNome()), CostruttoreDellaForesta.getNomiMissioniDaMostrare(casa));
        }
    }

    @Test
    void tornandoARiscuotereLaCittaDellIncaricoLampeggia() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(22)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
            MissioneAPassi medaglione = trova(IncaricoDiCombattimento.class);
            medaglione.aggiungiProprieta("PARAMETRO_" + IncaricoDiCombattimento.INCARICO, LADRI);
            medaglione.controllaPreLocazione();
            medaglione.segnaIntermezzoPassoMostrato("INCARICO");
            medaglione.controllaInLocazione();
            CoordinateMD fleena = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CITTA_FLEENA);

            // Sconfitti i ladri si torna in città, ma il gruppo è altrove: Fleena lampeggia
            medaglione.aggiungiProprieta("PASSO_CORRENTE", "RITORNO");
            Foresta.setPosizioneDelGruppo(RegistroMissioni.getLocazioneOccupata(medaglione));
            medaglione.controllaPreLocazione();
            assertTrue(CostruttoreDellaForesta.getCoordinateDaSegnalare().contains(fleena), "la città del mandante lampeggia");
            assertTrue(CostruttoreDellaForesta.getNomiMissioniDaMostrare(fleena).contains(medaglione.getNome()));

            // Tornati: ringraziamento, ricompensa, e il segnalino sparisce
            Foresta.setPosizioneDelGruppo(fleena);
            medaglione.controllaPreLocazione();
            medaglione.segnaIntermezzoPassoMostrato("RITORNO");
            medaglione.controllaInLocazione();
            assertTrue(medaglione.isCompleta());
            assertFalse(CostruttoreDellaForesta.getNomiMissioniDaMostrare(fleena).contains(medaglione.getNome()));
        }
    }
}
