package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LanciatoreDeiDadiTest {

    @Test
    void lePercentualiDiOgniClasseSommanoACento() {
        for (ClassePersonaggio classe : ClassePersonaggio.values()) {
            assertEquals(100, Arrays.stream(LanciatoreDeiDadi.getPercentualiPer(classe)).sum(), classe.name());
        }
    }
}
