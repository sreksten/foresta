package com.threeamigos.foresta.ui.testo;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il produttore di testo che prende i glifi dall'immagine del font: altezze, larghezze misurate sui pixel, caratteri
 * mancanti e trasparenza del colore.
 */
class DoomdarkTextProducerTest {

    private static final DoomdarkFont[] FONT = {DoomdarkFont.piccolo(), DoomdarkFont.medio()};

    @Test
    void unTestoEAltoQuantoLaRigaPienaDelFontPerOgniRiga() {
        for (DoomdarkFont font : FONT) {
            BufferedImage una = (BufferedImage) DoomdarkTextProducer.getImage("Foresta", font);
            assertEquals(font.getHeight(), una.getHeight());
            assertEquals(FontTool.getWidth(font, "Foresta"), una.getWidth());
            BufferedImage righe = (BufferedImage) DoomdarkTextProducer.getImage("Foresta Antica Dei Cavalieri", font, 70);
            assertTrue(righe.getHeight() >= 2 * font.getHeight());
            assertEquals(0, righe.getHeight() % font.getHeight());
        }
    }

    @Test
    void unTestoVuotoEUnPixelLargoEAltoQuantoIlFont() {
        for (DoomdarkFont font : FONT) {
            BufferedImage immagine = (BufferedImage) DoomdarkTextProducer.getImage("", font);
            assertEquals(1, immagine.getWidth());
            assertEquals(font.getHeight(), immagine.getHeight());
        }
    }

    @Test
    void unCarattereCheNonCeSiSaltaSenzaEccezioniEIlTestoRestaLargoComeSenza() {
        for (DoomdarkFont font : FONT) {
            assertFalse(font.supportaIlCarattere('_'));
            BufferedImage con = (BufferedImage) assertDoesNotThrow(() -> DoomdarkTextProducer.getImage("Zo_", font));
            BufferedImage senza = (BufferedImage) DoomdarkTextProducer.getImage("Zo", font);
            assertEquals(senza.getWidth(), con.getWidth());
        }
    }

    @Test
    void laLarghezzaDelGlifoEQuellaMisurataSuiPixel() {
        DoomdarkFont piccolo = DoomdarkFont.piccolo();
        // Una i è più stretta di una M, e lo spazio, che non ha pixel accesi, ha la larghezza fissa
        assertTrue(piccolo.getGlyphWidth('i') < piccolo.getGlyphWidth('M'));
        assertEquals(4, piccolo.getGlyphWidth(' '));
        assertEquals(4, DoomdarkFont.medio().getGlyphWidth(' '));
    }

    @Test
    void ilFontHaLeMisureDelCorpoELeRigheInPiu() {
        DoomdarkFont piccolo = DoomdarkFont.piccolo();
        DoomdarkFont medio = DoomdarkFont.medio();
        assertEquals(8, piccolo.getAltezzaDelCorpo());
        assertEquals(16, medio.getAltezzaDelCorpo());
        assertEquals(DoomdarkFont.EXTRA_IN_CIMA_PICCOLO, piccolo.getRigheInCima());
        assertEquals(DoomdarkFont.EXTRA_IN_CIMA_MEDIO, medio.getRigheInCima());
        assertEquals(DoomdarkFont.EXTRA_IN_CIMA_PICCOLO + 8 + DoomdarkFont.EXTRA_IN_FONDO_PICCOLO, piccolo.getHeight());
        assertEquals(DoomdarkFont.EXTRA_IN_CIMA_MEDIO + 16 + DoomdarkFont.EXTRA_IN_FONDO_MEDIO, medio.getHeight());
        assertEquals(1, piccolo.getSpacing());
        assertEquals(1, piccolo.getPadding());
    }

    @Test
    void ilLivelloELAlfaDelColoreEIlColoreResta() {
        int giallo = 0xFFFFFF00;
        // 0 trasparente, 255 il colore pieno
        assertEquals(0, DoomdarkTextProducer.pixel(giallo, 0));
        assertEquals(giallo, DoomdarkTextProducer.pixel(giallo, 255));
        // In mezzo il colore non cambia, cambia l'alfa: sfuma sullo sfondo
        assertEquals(0x7FFFFF00, DoomdarkTextProducer.pixel(giallo, 127));
        assertEquals(0x40FFFF00, DoomdarkTextProducer.pixel(giallo, 64));
        // Un colore già un po' trasparente: le due alfa si moltiplicano
        assertEquals(0x40FFFF00, DoomdarkTextProducer.pixel(0x80FFFF00, 128));
    }

    @Test
    void iPixelOpachiDelTestoSonoDelColoreDelTestoNonDiUnGrigio() {
        int colore = DoomdarkColorModel.getColorModel(DoomdarkColorModel.Color.YELLOW).getRGB(1);
        BufferedImage immagine = (BufferedImage) DoomdarkTextProducer.getImage("A", FONT[0], DoomdarkColorModel.Color.YELLOW);
        boolean pieno = false;
        for (int y = 0; y < immagine.getHeight(); y++) {
            for (int x = 0; x < immagine.getWidth(); x++) {
                int p = immagine.getRGB(x, y);
                if ((p >>> 24) == 0xFF) {
                    pieno = true;
                    assertEquals(colore & 0xFFFFFF, p & 0xFFFFFF);
                }
            }
        }
        assertTrue(pieno);
    }
}
