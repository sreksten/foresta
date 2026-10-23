package com.threeamigos.foresta.motore;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import com.threeamigos.foresta.testi.ProduttoreDiTestiCasuale;

/**
 * La grammatica missioni.txt dà i nomi di ostaggi e capibanda.
 */
class NomiDelleMissioniTest {

    @Test
    void ostaggiECapibandaHannoNomiVari() {
        verificaNomi(ProduttoreDiTestiCasuale::nomeOstaggio, 10);
        verificaNomi(ProduttoreDiTestiCasuale::nomeCapobanda, 15);
    }

    @Test
    void qualcheCapobandaHaUnSoprannome() {
        boolean conSoprannome = false;
        for (int i = 0; i < 200 && !conSoprannome; i++) {
            conSoprannome = ProduttoreDiTestiCasuale.nomeCapobanda().contains(" ");
        }
        assertTrue(conSoprannome);
    }

    /**
     * I nomi maschili sono one-shot e condivisi (nomi.txt): finché ce ne sono non si ripetono, e quando sono finiti
     * ProduttoreDiTestiCasuale azzera le sezioni statiche e riparte, invece di lanciare IllegalArgumentException.
     */
    @Test
    void quandoINomiFinisconoSiRipartePerNonLanciare() {
        ProduttoreDiTestiCasuale.resetProduzioni();
        try {
            Set<String> visti = new HashSet<>();
            int primaRipetizione = -1;
            for (int i = 0; i < 500; i++) {
                String nome = ProduttoreDiTestiCasuale.nomeOstaggio();
                if (!visti.add(nome) && primaRipetizione < 0) {
                    primaRipetizione = i;
                }
            }
            assertTrue(primaRipetizione > 50, "i nomi dovrebbero finire prima di ripetersi, non subito: " + primaRipetizione);
            assertEquals(visti.size(), primaRipetizione, "nessun nome si ripete finché ce ne sono di nuovi");
        } finally {
            ProduttoreDiTestiCasuale.resetProduzioni();
        }
    }

    @Test
    void ancheIMaghiSiRiprendonoDopoLEsaurimento() {
        ProduttoreDiTestiCasuale.resetProduzioni();
        try {
            for (int i = 0; i < 500; i++) {
                String nome = ProduttoreDiTestiCasuale.rigaDiMissioni("NOME_MAGO");
                assertTrue(nome.startsWith("Mastro "), nome);
            }
        } finally {
            ProduttoreDiTestiCasuale.resetProduzioni();
        }
    }

    @Test
    void ancheINecromantiSiRiprendonoDopoLEsaurimento() {
        ProduttoreDiTestiCasuale.resetProduzioni();
        try {
            for (int i = 0; i < 500; i++) {
                String nome = ProduttoreDiTestiCasuale.rigaDiMissioni("NOME_NECROMANTE");
                assertFalse(nome.isEmpty());
                assertTrue(Character.isUpperCase(nome.charAt(0)), nome);
                assertFalse(nome.contains("[") || nome.contains("  "), nome);
            }
        } finally {
            ProduttoreDiTestiCasuale.resetProduzioni();
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
