package com.threeamigos.foresta.motore;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Stefano Reksten
 */
class GestoreProgressioneTest {

    @Test
    @DisplayName("Emissione tabella livelli")
    void testEmissioneTabellaLivelli() {
        for (int i = 2; i < 30; i++) {
            System.out.printf("Livello %2d: %10d%n", i, GestoreProgressione.getXpNecessariPerLivello(i));
        }
    }

}