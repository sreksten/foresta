package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.incarichi.IncaricoDiCombattimento;
import com.threeamigos.foresta.missioni.incarichi.LaTagliaSullaBanda;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sulla mappa lampeggiano le caselle delle missioni a passi in corso che il gruppo conosce: il covo dei ladri, il
 * bosco di una banda, il tempio di un pellegrino... Non i castelli, e non più a missione finita.
 */
class ScenarioCaselleSegnalateTest {

    private static final String LADRI = "CHIAVE=LADRI_DI_PROVA;TIPO=RECUPERO;ASPETTO=QUALUNQUE;MANDANTE=il derubato;NEMICO=LADRO;"
            + "NUMERO=4;LUOGO=GROTTA;MONETE=20;TITOLO=Recupera il medaglione;RICHIESTA=Mi hanno rubato il medaglione.;"
            + "BATTUTA=Lo riavrai.;RISPOSTA=Grazie.;VITTORIA=I ladri sono a terra.;RINGRAZIAMENTO=Il mio medaglione!;"
            + "RICORDO=In questa grotta i ladri nascondevano il medaglione.";

    @Test
    void ilCovoDeiLadriLampeggiaFinchéLaMissioneÈInCorso() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(22)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_FLEENA));
            IncaricoDiCombattimento medaglione = RegistroMissioni.getTutteLeMissioni().stream()
                    .filter(IncaricoDiCombattimento.class::isInstance).map(IncaricoDiCombattimento.class::cast)
                    .findFirst().orElseThrow(AssertionError::new);
            medaglione.aggiungiProprieta("PARAMETRO_" + IncaricoDiCombattimento.INCARICO, LADRI);
            medaglione.controllaPreLocazione();
            medaglione.segnaIntermezzoPassoMostrato("INCARICO");
            medaglione.controllaInLocazione();
            CoordinateMD covo = RegistroMissioni.getLocazioneOccupata(medaglione);
            assertNotNull(covo);
            assertEquals(TipoLocazione.GROTTA, Foresta.getLocazione(covo), "una grotta come le altre");
            assertTrue(CostruttoreDellaForesta.getCoordinateDaSegnalare().contains(covo));

            // I castelli no, anche se conosciuti
            RegistroMissioni.getMissionePrincipale().getMissioniSecondarie().forEach(Missione::controllaPreLocazione);
            CoordinateMD castello = Foresta.getCoordinateLocazioneUnica(TipoLocazione.CASTELLO_STREGA);
            Foresta.setLocazioneConosciuta(castello);
            assertFalse(CostruttoreDellaForesta.getCoordinateDaSegnalare().contains(castello));
        }
    }

    @Test
    void ilBoscoDellaBandaLampeggiaDuranteLaCacciaENonDopo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(71)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            LaTagliaSullaBanda taglia = RegistroMissioni.getTutteLeMissioni().stream().filter(LaTagliaSullaBanda.class::isInstance)
                    .map(LaTagliaSullaBanda.class::cast).findFirst().orElseThrow(AssertionError::new);
            assertFalse(taglia.isAttiva());
            taglia.controllaPreLocazione();
            taglia.segnaIntermezzoPassoMostrato("INCARICO");
            taglia.controllaInLocazione();
            CoordinateMD covo = taglia.getCovo();
            assertTrue(CostruttoreDellaForesta.getCoordinateDaSegnalare().contains(covo));

            Foresta.setPosizioneDelGruppo(covo);
            for (int i = 0; i < LaTagliaSullaBanda.HOBGOBLIN; i++) {
                partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.HOBGOBLIN));
            }
            taglia.controllaPostLocazione();
            taglia.completaMissione();
            assertFalse(CostruttoreDellaForesta.getCoordinateDaSegnalare().contains(covo), "a missione finita non lampeggia più");
        }
    }
}
