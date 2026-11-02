package com.threeamigos.foresta.ui.componenti;

import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Un elenco si scorre (e l'aiuto delle schermate di scambio lo dice) solo se è più alto dello spazio in cui lo si
 * disegna.
 */
class ComponenteScorrevoleTest {

    @Test
    void siScorreSoloSeLElencoNonStaNelloSpazio() {
        ComponenteScorrevole<String> elenco = new ComponenteScorrevole<>(300, 10, 2);
        for (int i = 0; i < 5; i++) {
            elenco.creaNodo("Riga " + i, DoomdarkFont.medio(), DoomdarkColorModel.Color.LIGHT_GRAY,
                    null, null, null, "riga " + i);
        }
        assertFalse(elenco.isScorrevole(1000));
        assertTrue(elenco.isScorrevole(20));
    }

    private static ComponenteScorrevole<String> elencoDiProva() {
        ComponenteScorrevole<String> elenco = new ComponenteScorrevole<>(300, 10, 2);
        for (int i = 0; i < 5; i++) {
            elenco.creaNodo("Riga " + i, DoomdarkFont.medio(), DoomdarkColorModel.Color.LIGHT_GRAY,
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

    @Test
    void ilMassimoDiScorrimentoELEccedenzaDellElencoSulloSpazio() {
        ComponenteScorrevole<String> elenco = elencoDiProva();
        int altezzaElenco = elenco.getMassimoScorrimento(0);
        assertTrue(altezzaElenco > 0);
        assertEquals(altezzaElenco - 20, elenco.getMassimoScorrimento(20));
        assertEquals(0, elenco.getMassimoScorrimento(1000));
        assertEquals(0, elenco.limitaOffset(1000, 50));
        assertEquals(altezzaElenco - 20, elenco.limitaOffset(20, 100000));
        assertEquals(0, elenco.limitaOffset(20, -5));
    }

    @Test
    void producendoConUnoScorrimentoQuelloSiAdattaAllElenco() {
        ComponenteScorrevole<String> elenco = elencoDiProva();
        ScorrimentoSuUnAsse scorrimento = new ScorrimentoSuUnAsse(System::nanoTime);
        elenco.produci(20, scorrimento);
        assertEquals(elenco.getMassimoScorrimento(20), scorrimento.getMassimo(), 0.001d);
        // Un elenco che ci sta tutto non scorre: lo scorrimento torna entro lo 0
        scorrimento.scorriFinoA(50.0d, 0.0d);
        elenco.produci(1000, scorrimento);
        assertEquals(0.0d, scorrimento.getMassimo(), 0.001d);
        assertEquals(0.0d, scorrimento.getPosizioneDiArrivo(), 0.001d);
    }
}
