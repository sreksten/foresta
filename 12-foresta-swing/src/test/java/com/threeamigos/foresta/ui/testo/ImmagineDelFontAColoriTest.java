package com.threeamigos.foresta.ui.testo;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La lettura dei font a colori scritti in un'immagine: la stessa griglia di quelli a grigi, ma i pixel tengono il loro
 * colore, il nero puro è trasparente e il rosso puro segna un glifo che manca.
 */
class ImmagineDelFontAColoriTest {

    private static final int ROSSO = 0xFF0000;
    private static final int PASSO_X = 5;
    private static final int PASSO_Y = 6;
    private static final int ALTEZZA_RIQUADRO = PASSO_Y - 1;
    private static final int LARGHEZZA_DELLO_SPAZIO = 3;

    private static BufferedImage griglia() {
        BufferedImage immagine = new BufferedImage(1 + CaratteriDelFont.colonne() * PASSO_X,
                1 + CaratteriDelFont.RIGHE.length * PASSO_Y, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < immagine.getWidth(); x++) {
            for (int y = 0; y < immagine.getHeight(); y++) {
                if (x % PASSO_X == 0 || y % PASSO_Y == 0) {
                    immagine.setRGB(x, y, ROSSO);
                }
            }
        }
        return immagine;
    }

    private static void punto(BufferedImage immagine, int riga, int colonna, int x, int y, int rgb) {
        immagine.setRGB(1 + colonna * PASSO_X + x, 1 + riga * PASSO_Y + y, rgb);
    }

    @Test
    void ilGlifoTieneIColoriEHaLaLarghezzaDelSuoPixelPiuADestra() {
        BufferedImage immagine = griglia();
        punto(immagine, 0, 0, 0, 0, 0x008C00);
        punto(immagine, 0, 0, 2, 1, 0x737373);
        ImmagineDelFontAColori font = ImmagineDelFontAColori.daImmagine(immagine, LARGHEZZA_DELLO_SPAZIO);
        GlifoAColori a = font.getGlifo('A');
        assertEquals(3, a.getLarghezza());
        assertEquals(ALTEZZA_RIQUADRO, a.getAltezza());
        assertEquals(ALTEZZA_RIQUADRO, font.getAltezza());
        assertEquals(0xFF008C00, a.getPixel(0, 0));
        assertEquals(0xFF737373, a.getPixel(2, 1));
        assertEquals(0, a.getPixel(1, 0), "il nero puro è trasparente");
    }

    @Test
    void unPixelTrasparenteNonContaEUnAlfaParzialeRestaTale() {
        BufferedImage immagine = new BufferedImage(1 + CaratteriDelFont.colonne() * PASSO_X,
                1 + CaratteriDelFont.RIGHE.length * PASSO_Y, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < immagine.getWidth(); x++) {
            for (int y = 0; y < immagine.getHeight(); y++) {
                if (x % PASSO_X == 0 || y % PASSO_Y == 0) {
                    immagine.setRGB(x, y, 0xFF000000 | ROSSO);
                }
            }
        }
        immagine.setRGB(1, 1, 0x80008C00);
        immagine.setRGB(4, 1, 0x00FF0000);
        GlifoAColori a = ImmagineDelFontAColori.daImmagine(immagine, LARGHEZZA_DELLO_SPAZIO).getGlifo('A');
        assertEquals(1, a.getLarghezza(), "un rosso trasparente non è un segnaposto e non allarga il glifo");
        assertEquals(0x80008C00, a.getPixel(0, 0));
    }

    @Test
    void unPixelRossoFaMancareIlGlifoELoSpazioHaLaLarghezzaDataVuoto() {
        BufferedImage immagine = griglia();
        punto(immagine, 0, 1, 0, 0, 0xFFFFFF);
        punto(immagine, 0, 1, 1, 1, ROSSO);
        ImmagineDelFontAColori font = ImmagineDelFontAColori.daImmagine(immagine, LARGHEZZA_DELLO_SPAZIO);
        assertFalse(font.supportaIlCarattere('B'));
        assertTrue(font.getCaratteriMancanti().contains('B'));
        assertThrows(DoomdarkFont.UnsupportedCharacterException.class, () -> font.getGlifo('B'));
        // Un riquadro vuoto è un glifo che manca, tranne lo spazio
        assertFalse(font.supportaIlCarattere('C'));
        assertEquals(LARGHEZZA_DELLO_SPAZIO, font.getLarghezzaDelGlifo(' '));
    }

    @Test
    void unaImmagineCheNonEUnaGrigliaDaErrore() {
        assertThrows(IllegalStateException.class,
                () -> ImmagineDelFontAColori.daImmagine(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), 3));
    }
}
