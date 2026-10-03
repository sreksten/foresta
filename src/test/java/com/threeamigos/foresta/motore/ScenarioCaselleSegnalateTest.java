package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.LaTagliaSullaBanda;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sulla mappa lampeggiano le caselle delle missioni a passi in corso che il gruppo conosce: il covo dei ladri, il
 * bosco di una banda, il tempio di un pellegrino... Non i castelli, e non più a missione finita.
 */
class ScenarioCaselleSegnalateTest {

    @Test
    void ilCovoDelMedaglioneLampeggiaFinchéLaMissioneÈInCorso() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(22)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_FLEENA));
            CoordinateMD covo = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.GROTTA_RECUPERA_IL_MEDAGLIONE);
            assertTrue(Foresta.getCoordinateDaSegnalare().contains(covo));

            // I castelli no, anche se conosciuti
            RegistroMissioni.getMissionePrincipale().getMissioniSecondarie().forEach(Missione::controllaPreLocazione);
            CoordinateMD castello = Foresta.getCoordinateLocazioneUnica(ClassiLocazione.CASTELLO_STREGA);
            Foresta.setLocazioneConosciuta(castello);
            assertFalse(Foresta.getCoordinateDaSegnalare().contains(castello));
        }
    }

    @Test
    void ilBoscoDellaBandaLampeggiaDuranteLaCacciaENonDopo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(71)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(ClassiLocazione.CITTA_NYENA));
            LaTagliaSullaBanda taglia = RegistroMissioni.getTutteLeMissioni().stream().filter(LaTagliaSullaBanda.class::isInstance)
                    .map(LaTagliaSullaBanda.class::cast).findFirst().orElseThrow(AssertionError::new);
            assertFalse(taglia.isAttiva());
            taglia.controllaPreLocazione();
            taglia.segnaIntermezzoPassoMostrato("INCARICO");
            taglia.controllaInLocazione();
            CoordinateMD covo = taglia.getCovo();
            assertTrue(Foresta.getCoordinateDaSegnalare().contains(covo));

            partita.gruppo().setCoordinate(covo);
            for (int i = 0; i < LaTagliaSullaBanda.HOBGOBLIN; i++) {
                partita.pubblica(new InternoAvversarioSconfitto(ClassePersonaggio.HOBGOBLIN));
            }
            taglia.controllaPostLocazione();
            taglia.completaMissione();
            assertFalse(Foresta.getCoordinateDaSegnalare().contains(covo), "a missione finita non lampeggia più");
        }
    }
}
