package com.threeamigos.foresta.tipi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class TipoIntermezzoTest {

    @Test
    void nessunIntermezzoFissoEDiProva() {
        for (TipoIntermezzo intermezzo : TipoIntermezzo.values()) {
            assertFalse(intermezzo.isDiProva(), intermezzo.name());
        }
    }
}
