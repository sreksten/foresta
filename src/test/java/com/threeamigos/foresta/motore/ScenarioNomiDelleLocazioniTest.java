package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.locazioni.Locanda;
import com.threeamigos.foresta.locazioni.Tempio;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.tools.GestoreSalvataggi;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Città, castelli, locande e templi hanno il nome nella casella (LocazioneMD.NOME), e la mappa lo mostra se il
 * gruppo conosce la casella (Foresta.getNomeDaMostrare).
 */
class ScenarioNomiDelleLocazioniTest {

    @Test
    void ogniLocazioneConUnNomeLoHaNellaCasellaELoMostraSoloSeConosciuta() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(92)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            RegistroMissioni.getMissionePrincipale().getMissioniSecondarie().forEach(Missione::controllaPreLocazione);

            CoordinateMD ruuna = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CITTA_RUUNA);
            CoordinateMD strega = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CASTELLO_STREGA);
            CoordinateMD tempio = prima(ClassiLocazione.TEMPIO);
            CoordinateMD locanda = prima(ClassiLocazione.LOCANDA);
            CoordinateMD bosco = prima(ClassiLocazione.BOSCO);

            assertEquals("la città di Ruuna", Foresta.getLocazioneMD(ruuna).getNome());
            assertEquals("il Maniero del Malefizio", Foresta.getLocazioneMD(strega).getNome());
            assertNotNull(Foresta.getLocazioneMD(tempio).getNome(), "il tempio ha il nome da quando nasce");
            assertEquals(Tempio.getNome(tempio), Foresta.getLocazioneMD(tempio).getNome());
            assertNotNull(Foresta.getLocazioneMD(locanda).getNome());
            assertEquals(Locanda.getNome(Foresta.getLocazioneMD(locanda)), Foresta.getLocazioneMD(locanda).getNome());
            // La locanda della città ha il suo nome, che non è quello della città
            String locandaDiRuuna = Locanda.getNome(Foresta.getLocazioneMD(ruuna));
            assertNotNull(locandaDiRuuna);
            assertNotEquals("la città di Ruuna", locandaDiRuuna);

            for (CoordinateMD coordinate : new CoordinateMD[]{ruuna, strega, tempio, locanda, bosco}) {
                Foresta.getLocazioneMD(coordinate).rimuoviProprieta(com.threeamigos.foresta.motore.modellodati.LocazioneMD.CONOSCIUTA);
                assertNull(Foresta.getNomeDaMostrare(coordinate), "non conosciuta: " + coordinate);
                Foresta.setLocazioneConosciuta(coordinate);
            }
            assertEquals("la città di Ruuna", Foresta.getNomeDaMostrare(ruuna));
            assertEquals("il Maniero del Malefizio", Foresta.getNomeDaMostrare(strega));
            assertNull(Foresta.getNomeDaMostrare(bosco), "un bosco non ha nome");
            assertNull(Foresta.getNomeDaMostrare(new CoordinateMD(-1, 0)));

            String nomeDelTempio = Foresta.getNomeDaMostrare(tempio);
            String nomeDellaLocanda = Foresta.getNomeDaMostrare(locanda);
            GestoreSalvataggi.salva(Comando.NUMERO_2);
            assertTrue(GestoreSalvataggi.leggi(Comando.NUMERO_2));
            assertEquals(nomeDelTempio, Foresta.getNomeDaMostrare(tempio));
            assertEquals(nomeDellaLocanda, Foresta.getNomeDaMostrare(locanda));
            assertEquals("la città di Ruuna", Foresta.getNomeDaMostrare(ruuna));
            assertEquals(locandaDiRuuna, Locanda.getNome(Foresta.getLocazioneMD(ruuna)));

            // Un castello sconfitto diventa rovine, che prendono il nome dal castello
            Foresta.distruggiLocazioneUnica(ClassiLocazione.CASTELLO_STREGA, ClassiLocazione.ROVINE);
            assertEquals("le Rovine del Maniero del Malefizio", Foresta.getNomeDaMostrare(strega));
        }
    }

    private static CoordinateMD prima(ClassiLocazione classe) {
        for (int x = 0; x < Foresta.getDimensioneX(); x++) {
            for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                if (Foresta.getLocazione(x, y) == classe) {
                    return new CoordinateMD(x, y);
                }
            }
        }
        throw new AssertionError("nessuna casella " + classe);
    }
}
