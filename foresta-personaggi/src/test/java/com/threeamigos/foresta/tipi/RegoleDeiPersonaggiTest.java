package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.artefatti.CostantiArtefatti;
import com.threeamigos.foresta.tipi.*;
import org.junit.jupiter.api.Test;
import java.util.EnumSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Le regole dei tipi di personaggio: le classi di genere diverso hanno lo stesso ruolo.
 */
class RegoleDeiPersonaggiTest {

    @Test
    void leClassiDiGenereDiversoHannoLoStessoRuolo() {
        TipoPersonaggio[][] coppie = {
                {TipoPersonaggio.MAGO, TipoPersonaggio.MAGA}, {TipoPersonaggio.ELFO, TipoPersonaggio.ELFA},
                {TipoPersonaggio.LADRO, TipoPersonaggio.LADRA}, {TipoPersonaggio.GUERRIERO, TipoPersonaggio.GUERRIERA},
                {TipoPersonaggio.BARDO, TipoPersonaggio.CANTASTORIE}, {TipoPersonaggio.SACERDOTE, TipoPersonaggio.SACERDOTESSA}};
        for (TipoPersonaggio[] coppia : coppie) {
            assertSame(coppia[0], coppia[0].getRuolo());
            assertSame(coppia[0], coppia[1].getRuolo());
            assertTrue(coppia[1].stessoRuoloDi(coppia[0]));
        }
        assertSame(TipoPersonaggio.GOBLIN, TipoPersonaggio.GOBLIN.getRuolo(), "le altre classi hanno il loro ruolo");
        assertFalse(TipoPersonaggio.MAGO.stessoRuoloDi(TipoPersonaggio.ELFA));
    }
}
