package com.threeamigos.foresta.effettidistato.tipi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ogni costante delle enum del modulo si ritrova dal suo nome, come fanno i salvataggi (LettoreCampi.enumerato).
 */
class CostantiDalNomeTest {

    @Test
    void ogniCostanteSiRitrovaDalSuoNome() {
        for (Class<? extends Enum> classe : new Class[]{TipoEffettoDiStato.class}) {
            assertTrue(classe.getEnumConstants().length > 0, classe + " non ha costanti");
            for (Object costante : classe.getEnumConstants()) {
                Enum<?> valore = (Enum<?>) costante;
                assertSame(valore, Enum.valueOf(classe, valore.name()), classe.getSimpleName() + "." + valore.name());
            }
        }
    }
}
