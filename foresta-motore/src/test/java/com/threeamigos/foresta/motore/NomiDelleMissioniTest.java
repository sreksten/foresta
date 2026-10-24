package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.testi.ProduttoreDiMissioniCasuale;
import com.threeamigos.foresta.testi.ProduttoriDiTesti;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La grammatica missioni.txt dà i nomi di ostaggi e capibanda.
 */
class NomiDelleMissioniTest {

    @Test
    void ostaggiECapibandaHannoNomiVari() {
        verificaNomi(ProduttoreDiMissioniCasuale::nomeOstaggio, 10);
        verificaNomi(ProduttoreDiMissioniCasuale::nomeCapobanda, 15);
    }

    @Test
    void qualcheCapobandaHaUnSoprannome() {
        boolean conSoprannome = false;
        for (int i = 0; i < 200 && !conSoprannome; i++) {
            conSoprannome = ProduttoreDiMissioniCasuale.nomeCapobanda().contains(" ");
        }
        assertTrue(conSoprannome);
    }

    /**
     * I nomi maschili sono one-shot e condivisi (nomi.txt): finché ce ne sono non si ripetono, e quando sono finiti
     * ProduttoreDiMissioniCasuale azzera le sezioni statiche e riparte, invece di lanciare IllegalArgumentException.
     */
    @Test
    void quandoINomiFinisconoSiRipartePerNonLanciare() {
        ProduttoriDiTesti.resetProduzioni();
        try {
            Set<String> visti = new HashSet<>();
            int primaRipetizione = -1;
            for (int i = 0; i < 500; i++) {
                String nome = ProduttoreDiMissioniCasuale.nomeOstaggio();
                if (!visti.add(nome) && primaRipetizione < 0) {
                    primaRipetizione = i;
                }
            }
            assertTrue(primaRipetizione > 50, "i nomi dovrebbero finire prima di ripetersi, non subito: " + primaRipetizione);
            assertEquals(visti.size(), primaRipetizione, "nessun nome si ripete finché ce ne sono di nuovi");
        } finally {
            ProduttoriDiTesti.resetProduzioni();
        }
    }

    @Test
    void ancheIMaghiSiRiprendonoDopoLEsaurimento() {
        ProduttoriDiTesti.resetProduzioni();
        try {
            for (int i = 0; i < 500; i++) {
                String nome = ProduttoreDiMissioniCasuale.rigaDiMissioni("NOME_MAGO");
                assertTrue(nome.startsWith("Mastro "), nome);
            }
        } finally {
            ProduttoriDiTesti.resetProduzioni();
        }
    }

    @Test
    void ancheINecromantiSiRiprendonoDopoLEsaurimento() {
        ProduttoriDiTesti.resetProduzioni();
        try {
            for (int i = 0; i < 500; i++) {
                String nome = ProduttoreDiMissioniCasuale.rigaDiMissioni("NOME_NECROMANTE");
                assertFalse(nome.isEmpty());
                assertTrue(Character.isUpperCase(nome.charAt(0)), nome);
                assertFalse(nome.contains("[") || nome.contains("  "), nome);
            }
        } finally {
            ProduttoriDiTesti.resetProduzioni();
        }
    }

    private static void verificaNomi(Supplier<String> generatore, int diversiAlmeno) {
        Set<String> nomi = new HashSet<>();
        for (int i = 0; i < 300; i++) {
            String nome = generatore.get();
            assertFalse(nome.isEmpty());
            assertTrue(Character.isUpperCase(nome.charAt(0)), nome);
            assertFalse(nome.contains("[") || nome.contains("{") || nome.contains("  "), nome);
            nomi.add(nome);
        }
        assertTrue(nomi.size() >= diversiAlmeno, "nomi diversi: " + nomi);
    }
}
