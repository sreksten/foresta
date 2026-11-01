package com.threeamigos.foresta.motore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaccontoTest {

    @Test
    void laStoriaEIFinaliSiLeggonoDalFileDeiTesti() {
        assertEquals(5, Racconto.getStoria().size());
        assertTrue(Racconto.getStoria().get(0).startsWith("molto, molto tempo fa"));
        assertEquals(1, Racconto.getFinaleDellaSconfitta().size());
        assertEquals(5, Racconto.getFinaleDellaVittoria().size());
        assertEquals("", Racconto.getFinaleDellaVittoria().get(4));
    }
}
