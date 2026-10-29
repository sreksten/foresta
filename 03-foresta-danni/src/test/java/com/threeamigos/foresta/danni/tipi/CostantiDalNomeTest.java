package com.threeamigos.foresta.danni.tipi;

import com.threeamigos.foresta.tipi.TipoInterazioneConEffettiDiStato;
import com.threeamigos.foresta.tipi.CategoriaDanno;
import com.threeamigos.foresta.tipi.TipoDanno;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ogni costante delle enum del modulo si ritrova dal suo nome, come fanno i salvataggi (LettoreCampi.enumerato).
 */
class CostantiDalNomeTest {

    @Test
    void ogniCostanteSiRitrovaDalSuoNome() {
        for (Class<? extends Enum> classe : new Class[]{TipoDanno.class, CategoriaDanno.class, TipoInterazioneConEffettiDiStato.class}) {
            assertTrue(classe.getEnumConstants().length > 0, classe + " non ha costanti");
            for (Object costante : classe.getEnumConstants()) {
                Enum<?> valore = (Enum<?>) costante;
                assertSame(valore, Enum.valueOf(classe, valore.name()), classe.getSimpleName() + "." + valore.name());
            }
        }
    }
}
