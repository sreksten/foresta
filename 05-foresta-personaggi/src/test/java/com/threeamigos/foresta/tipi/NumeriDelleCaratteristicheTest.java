package com.threeamigos.foresta.tipi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * I moltiplicatori a parole: la virgola all'italiana e niente zeri inutili.
 */
class NumeriDelleCaratteristicheTest {

    @Test
    void ilNumeroInTestoToglieGliZeriInutili() {
        assertEquals("1,5", CaratteristicaDiClasse.numeroInTesto(1.5d));
        assertEquals("2", CaratteristicaDiClasse.numeroInTesto(2.0d));
        assertEquals("0,25", CaratteristicaDiClasse.numeroInTesto(0.25d));
        assertEquals("10", CaratteristicaDiClasse.numeroInTesto(10.0d));
    }
}
