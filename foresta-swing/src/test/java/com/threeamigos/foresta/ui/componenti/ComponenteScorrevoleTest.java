package com.threeamigos.foresta.ui.componenti;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFontMedium;

/**
 * Un elenco si scorre (e l'aiuto delle schermate di scambio lo dice) solo se è più alto dello spazio in cui lo si
 * disegna.
 */
class ComponenteScorrevoleTest {

    @Test
    void siScorreSoloSeLElencoNonStaNelloSpazio() {
        ComponenteScorrevole<String> elenco = new ComponenteScorrevole<>(300, 10, 2);
        for (int i = 0; i < 5; i++) {
            elenco.creaNodo("Riga " + i, DoomdarkFontMedium.getInstance(), DoomdarkColorModel.Color.LIGHT_GRAY,
                    null, null, null, "riga " + i);
        }
        assertFalse(elenco.isScorrevole(1000));
        assertTrue(elenco.isScorrevole(20));
    }
}
