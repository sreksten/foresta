package com.threeamigos.foresta.ui.componenti;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.awt.Image;
import java.awt.image.BufferedImage;
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

    private static ComponenteScorrevole<String> elencoDiProva() {
        ComponenteScorrevole<String> elenco = new ComponenteScorrevole<>(300, 10, 2);
        for (int i = 0; i < 5; i++) {
            elenco.creaNodo("Riga " + i, DoomdarkFontMedium.getInstance(), DoomdarkColorModel.Color.LIGHT_GRAY,
                    null, null, null, "riga " + i);
        }
        return elenco;
    }

    private static boolean haPixelDisegnati(Image immagine) {
        BufferedImage b = (BufferedImage) immagine;
        for (int y = 0; y < b.getHeight(); y++) {
            for (int x = 0; x < b.getWidth(); x++) {
                if ((b.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void ilTestoSiVedeGiaAlPrimoDisegno() {
        assertTrue(haPixelDisegnati(elencoDiProva().produci(1000, 0)));
    }

    @Test
    void conLoStessoContenutoLaPorzioneSiRiusa() {
        Image prima = elencoDiProva().produci(30, 5);
        assertSame(prima, elencoDiProva().produci(30, 5));
    }
}
