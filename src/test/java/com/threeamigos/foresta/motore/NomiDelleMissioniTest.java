package com.threeamigos.foresta.motore;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La grammatica missioni.txt dà i nomi di ostaggi, bardi e capibanda.
 */
class NomiDelleMissioniTest {

    @Test
    void ostaggiBardiECapibandaHannoNomiVari() {
        verificaNomi(ProduttoreDiTestiCasuale::nomeOstaggio, 10);
        verificaNomi(ProduttoreDiTestiCasuale::nomeBardo, 10);
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
