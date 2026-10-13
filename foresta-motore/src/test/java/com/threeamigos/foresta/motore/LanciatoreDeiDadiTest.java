package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.tipi.TipoPersonaggio;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LanciatoreDeiDadiTest {

    @Test
    void lePercentualiDiOgniClasseSommanoACento() {
        for (TipoPersonaggio classe : TipoPersonaggio.values()) {
            assertEquals(100, Arrays.stream(LanciatoreDeiDadi.getPercentualiPer(classe)).sum(), classe.name());
        }
    }
}
