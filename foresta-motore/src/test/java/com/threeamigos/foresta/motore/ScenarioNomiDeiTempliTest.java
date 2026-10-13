package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.locazioni.Tempio;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.strumenti.Misc;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I templi hanno un nome preso da templi.txt, diverso per ognuno, che si salva con la casella.
 */
class ScenarioNomiDeiTempliTest {

    @Test
    void laGrammaticaDaNomiConLArticoloEVari() {
        Set<String> nomi = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            String nome = ProduttoreDiTestiCasuale.nomeTempio();
            assertTrue(nome.matches("(il|lo|la|l') ?\\S.*"), nome);
            assertFalse(nome.contains("[") || nome.contains("{") || nome.contains("  "), nome);
            assertTrue(nome.contains(" del") || nome.contains(" dell"), nome);
            nomi.add(nome);
        }
        assertTrue(nomi.size() > 100, "nomi diversi: " + nomi.size());
    }

    @Test
    void ogniTempioHaIlSuoNomeCheSiSalvaESiRilegge() {
        try (PartitaDiTest partita = PartitaDiTest.nuova(91)) {
            partita.iniziaCon("Arsenio", Comando.MASCHIO, Comando.GUERRIERO,
                    () -> partita.spostaGruppoIn(TipoLocazione.CITTA_NYENA));
            List<CoordinateMD> templi = new ArrayList<>();
            for (int x = 0; x < Foresta.getDimensioneX(); x++) {
                for (int y = 0; y < Foresta.getDimensioneY(); y++) {
                    if (Foresta.getLocazione(x, y) == TipoLocazione.TEMPIO) {
                        templi.add(new CoordinateMD(x, y));
                    }
                }
            }
            assertTrue(templi.size() > 1);
            List<String> nomi = new ArrayList<>();
            for (CoordinateMD tempio : templi) {
                String nome = Tempio.getNome(tempio);
                assertEquals(nome, Tempio.getNome(tempio), "il nome non cambia");
                nomi.add(nome);
            }
            assertEquals(nomi.size(), new HashSet<>(nomi).size(), "nomi tutti diversi: " + nomi);

            partita.salva(Comando.NUMERO_2);
            assertTrue(partita.leggi(Comando.NUMERO_2));
            for (int i = 0; i < templi.size(); i++) {
                assertEquals(nomi.get(i), Foresta.getLocazioneMD(templi.get(i)).getNome(), "dopo il caricamento");
            }

            // Entrando, il tempio si presenta col suo nome
            CoordinateMD primo = templi.get(0);
            partita.gruppo().setCoordinate(primo);
            Foresta.costruisciIstanza(primo).descrivi(partita.gruppo(), GruppoAvversario.getIstanza());
            String atteso = "Qui, " + Misc.conPreposizione("in", nomi.get(0)) + ", ";
            assertTrue(partita.testi().stream().anyMatch(t -> t.startsWith(atteso)), atteso + " in " + partita.testi());
        }
    }
}
