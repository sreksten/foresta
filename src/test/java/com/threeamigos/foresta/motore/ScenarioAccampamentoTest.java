package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.intermezzi.BattutaProgrammata;
import com.threeamigos.foresta.intermezzi.IntermezzoAccampamento;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.motore.modellodati.IntermezziMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.personaggi.Guerriero;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gli intermezzi dell'accampamento: ognuno dei tre accampamenti in cui scattano ha la sua scena, poi non scattano più.
 */
class ScenarioAccampamentoTest {

    @Test
    void ogniAccampamentoHaLaSuaScenaFinoAlTerzo() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(221)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.LADRO, () -> { });
            partita.gruppo().aggiungiPersonaggio(new Guerriero("Sentinella", 1));
            IntermezziMD intermezzi = ModelloDati.getIstanza().getIntermezziMD();
            List<String> scene = new ArrayList<>();
            for (int accampamento = 1; accampamento <= 3; accampamento++) {
                intermezzi.incrementaNumeroAccampamenti();
                IntermezzoAccampamento intermezzo = new IntermezzoAccampamento();
                assertTrue(intermezzo.deveScattare(MomentoIntermezzo.ACCAMPAMENTO));
                scene.add(intermezzo.getPagine().get(0).getBattuteProgrammate().stream()
                        .map(BattutaProgrammata::getBattuta).map(b -> b.getTesto()).collect(Collectors.joining(" ")));
            }
            assertTrue(scene.get(0).contains("scarafaggio"), scene.get(0));
            assertTrue(scene.get(1).contains("stazione da battaglia"), scene.get(1));
            assertTrue(scene.get(2).contains("panico"), scene.get(2));
            assertEquals(3, new HashSet<>(scene).size());

            intermezzi.incrementaNumeroAccampamenti();
            assertFalse(new IntermezzoAccampamento().deveScattare(MomentoIntermezzo.ACCAMPAMENTO));
        }
    }
}
