package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.danni.tipi.TipoDanno;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le regole dei tipi dei poteri: categoria, portata e tipo di danno degli incantesimi.
 */
class RegoleDeiPoteriTest {

    @Test
    void ognunIncantesimoHaCategoriaPortataETipoDanno() {
        for (TipoIncantesimo incantesimo : TipoIncantesimo.values()) {
            assertNotNull(incantesimo.getCategoriaIncantesimo(), incantesimo.name());
            assertNotNull(incantesimo.getPortata(), incantesimo.name());
        }
        assertSame(TipoDanno.FUOCO, TipoIncantesimo.FUOCO.getTipoDanno());
        assertNull(TipoIncantesimo.RESURREZIONE.getTipoDanno(), "un incantesimo benefico non fa danni");
    }
}
