package com.threeamigos.foresta.attributi.tipi;

import com.threeamigos.foresta.modificatori.tipi.BersaglioModificatore;
import com.threeamigos.foresta.modificatori.tipi.TipoModificatore;
import com.threeamigos.foresta.modificatori.tipi.UnitaDurataModificatore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ogni costante delle enum del modulo si ritrova dal suo nome, come fanno i salvataggi (LettoreCampi.enumerato).
 */
class CostantiDalNomeTest {

    @Test
    void ogniCostanteSiRitrovaDalSuoNome() {
        for (Class<? extends Enum> classe : new Class[]{TipoAttributo.class, CategoriaAttributo.class, BersaglioModificatore.class, TipoModificatore.class, UnitaDurataModificatore.class}) {
            assertTrue(classe.getEnumConstants().length > 0, classe + " non ha costanti");
            for (Object costante : classe.getEnumConstants()) {
                Enum<?> valore = (Enum<?>) costante;
                assertSame(valore, Enum.valueOf(classe, valore.name()), classe.getSimpleName() + "." + valore.name());
            }
        }
    }
}
