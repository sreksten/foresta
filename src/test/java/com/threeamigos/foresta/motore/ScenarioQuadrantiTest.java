package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.missioni.Missione;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le città e i castelli degli alleati del Drago non sono sparsi a caso: uno per quadrante. E le radure sono poche.
 */
class ScenarioQuadrantiTest {

    private static final ClassiLocazione[] CITTA = {
            ClassiLocazione.CITTA_NYENA, ClassiLocazione.CITTA_MALGAARD, ClassiLocazione.CITTA_RUUNA, ClassiLocazione.CITTA_FLEENA};
    private static final ClassiLocazione[] CASTELLI = {
            ClassiLocazione.CASTELLO_IDRA, ClassiLocazione.CASTELLO_MINOTAURO, ClassiLocazione.CASTELLO_LICH, ClassiLocazione.CASTELLO_STREGA};

    @Test
    void unaCittaEUnCastelloPerQuadrante() {
        for (long seme = 81; seme <= 90; seme++) {
            try (PartitaDiTest partita = PartitaDiTest.nuova(seme)) {
                partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
                // I castelli li rivendicano le loro missioni al primo controllo della partita
                RegistroMissioni.getMissionePrincipale().getMissioniSecondarie().forEach(Missione::controllaPreLocazione);
                assertEquals(EnumSet.allOf(Quadrante.class), quadranti(CITTA), "città, seme " + seme);
                assertEquals(EnumSet.allOf(Quadrante.class), quadranti(CASTELLI), "castelli, seme " + seme);
            }
        }
    }

    @Test
    void ciSonoPocheRadure() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(95)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO, () -> { });
            int radure = 0;
            for (int x = 0; x < Foresta.getDimensioneX(); x++) {
                for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                    if (Foresta.getLocazione(x, y) == ClassiLocazione.RADURA) {
                        radure++;
                    }
                }
            }
            assertEquals((Foresta.getDimensioneX() + Foresta.getDimensioneY()) / 4, radure, "quante le grotte");
        }
    }

    private static Set<Quadrante> quadranti(ClassiLocazione[] locazioniUniche) {
        Set<Quadrante> quadranti = EnumSet.noneOf(Quadrante.class);
        for (ClassiLocazione locazione : locazioniUniche) {
            CoordinateMD coordinate = Foresta.getCoordinateLocazioneUnica(locazione);
            assertNotNull(coordinate, locazione + " non c'è");
            quadranti.add(Quadrante.di(coordinate));
        }
        return quadranti;
    }
}
