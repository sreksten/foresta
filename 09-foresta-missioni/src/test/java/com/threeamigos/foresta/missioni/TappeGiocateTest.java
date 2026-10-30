package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.missioni.incarichi.MissioneATappe;
import com.threeamigos.foresta.missioni.richieste.TappeRichieste;
import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le missioni a tappe, una per ogni forma di riga che la grammatica sa produrre (persona da cercare, bivio, chiavi con o senza scambio,
 * farsa, leggendario), giocate da un giocatore che gira a caso.
 */
class TappeGiocateTest {

    @AfterEach
    void ripulisce() {
        MondoDiProva.finito();
    }

    private static String forma(TappeRichieste r) {
        return r.isFarsa() + "/" + r.getBivio().isPresent() + "/" + r.hasChiavi() + "/" + r.isDiUnLeggendario() + "/" + r.getTappe().size();
    }

    @Test
    void ogniFormaDiRigaSiGioca() {
        Map<String, String> righe = new LinkedHashMap<>();
        for (int i = 0; i < 600; i++) {
            String riga = ProduttoreDiMissioniCasuale.rigaDiTappe(i % 4 == 0);
            righe.putIfAbsent(forma(TappeRichieste.da(riga)), riga);
        }
        assertTrue(righe.size() >= 4, righe.keySet().toString());
        Map<String, String> errori = new TreeMap<>();
        int seme = 100;
        for (String riga : righe.values()) {
            for (int giro = 0; giro < 2; giro++) {
                MondoDiProva.nuovo(++seme);
                RegistroMissioni.reimposta();
                RegistroMissioni.registrati();
                MissioneATappe missione = new MissioneATappe();
                missione.getModelloDati().setId("TAPPE" + seme);
                missione.aggiungiProprieta("PARAMETRO_RIGA", riga);
                RegistroMissioni.aggiungiMissioneSecondaria(missione);
                GiocatoreAutomatico giocatore = new GiocatoreAutomatico(seme);
                giocatore.gioca(120);
                errori.putAll(giocatore.getErrori());
            }
        }
        errori.keySet().removeIf(GiocatoreAutomaticoTest::isRumoreNoto);
        assertTrue(errori.isEmpty(), errori.toString());
    }
}
