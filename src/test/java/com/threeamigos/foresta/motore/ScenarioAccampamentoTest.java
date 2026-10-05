package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.intermezzi.BattutaProgrammata;
import com.threeamigos.foresta.intermezzi.IntermezzoAccampamento;
import com.threeamigos.foresta.intermezzi.MomentoIntermezzo;
import com.threeamigos.foresta.missioni.LealtaRichiesta;
import com.threeamigos.foresta.motore.modellodati.IntermezziMD;
import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.ModificatoreAttributo;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;
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
            Guerriero sentinella = new Guerriero("Sentinella", 1);
            // Già "leale" (vedi LaLealta): altrimenti la sua confidenza impedirebbe per sempre
            // all'intermezzo dell'accampamento di scattare (vedi IntermezzoAccampamento)
            sentinella.addModificatore(new ModificatoreAttributo(TipoAttributo.FORTUNA, TipoModificatore.QUANTITA_ASSOLUTA, 0, LealtaRichiesta.NOTA));
            partita.gruppo().aggiungiPersonaggio(sentinella);
            IntermezziMD intermezzi = ModelloDati.getIstanza().getIntermezziMD();
            List<String> scene = new ArrayList<>();
            for (int accampamento = 1; accampamento <= 3; accampamento++) {
                intermezzi.incrementaNumeroAccampamenti();
                RegistroIntermezzi.nuovoMomento();
                IntermezzoAccampamento intermezzo = new IntermezzoAccampamento();
                assertTrue(intermezzo.deveScattare(MomentoIntermezzo.ACCAMPAMENTO));
                scene.add(intermezzo.getPagine().get(0).getBattuteProgrammate().stream()
                        .map(BattutaProgrammata::getBattuta).map(b -> b.getTesto()).collect(Collectors.joining(" ")));
                RegistroIntermezzi.segnaScattato(intermezzo);
            }
            assertTrue(scene.get(0).contains("scarafaggio"), scene.get(0));
            assertTrue(scene.get(1).contains("stazione da battaglia"), scene.get(1));
            assertTrue(scene.get(2).contains("panico"), scene.get(2));
            assertEquals(3, new HashSet<>(scene).size());

            RegistroIntermezzi.nuovoMomento();
            intermezzi.incrementaNumeroAccampamenti();
            assertFalse(new IntermezzoAccampamento().deveScattare(MomentoIntermezzo.ACCAMPAMENTO));
        }
    }
}
