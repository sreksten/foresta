package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.eventi.interni.InternoAvversarioSconfitto;
import com.threeamigos.foresta.missioni.RegistroMissioni;
import com.threeamigos.foresta.missioni.incarichi.LaTagliaSullaBanda;
import com.threeamigos.foresta.mappa.CoordinateMD;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A ogni visita al covo la banda ricompare tutta: gli sconfitti si contano visita per visita, e quelli di visite diverse (magari dopo una
 * fuga) non si sommano fino a dare per sgominata una banda che è ancora viva.
 */
class SconfittiFraLeVisiteTest {

    private static void sconfitti(PartitaDiTest partita, int quanti) {
        for (int i = 0; i < quanti; i++) {
            partita.pubblica(new InternoAvversarioSconfitto(TipoPersonaggio.HOBGOBLIN));
        }
    }

    @Test
    void gliSconfittiDiVisiteDiverseNonSiSommano() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(71)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            LaTagliaSullaBanda taglia = RegistroMissioni.getTutteLeMissioni().stream().filter(LaTagliaSullaBanda.class::isInstance)
                    .map(LaTagliaSullaBanda.class::cast).findFirst().orElseThrow(AssertionError::new);
            taglia.controllaPreLocazione();
            taglia.segnaIntermezzoPassoMostrato("INCARICO");
            taglia.controllaInLocazione();
            CoordinateMD covo = taglia.getCovo();

            // Prima visita: la banda è di tre, se ne sconfiggono due e si fugge
            RegistroMissioni.getIncontroMissione(covo);
            partita.gruppo().setCoordinate(covo);
            sconfitti(partita, 2);
            taglia.controllaPostLocazione();
            assertEquals("CACCIA", taglia.getPassoCorrente());

            // Seconda visita: la banda è di nuovo intera, altri due non bastano anche se in tutto sono quattro
            RegistroMissioni.getIncontroMissione(covo);
            sconfitti(partita, 2);
            taglia.controllaPostLocazione();
            assertEquals("CACCIA", taglia.getPassoCorrente(), "quattro sconfitti in due visite non sgominano una banda di tre");

            // Terza visita: tutta la banda in una volta
            RegistroMissioni.getIncontroMissione(covo);
            sconfitti(partita, 3);
            taglia.controllaPostLocazione();
            assertEquals("RITORNO", taglia.getPassoCorrente());
        }
    }
}
