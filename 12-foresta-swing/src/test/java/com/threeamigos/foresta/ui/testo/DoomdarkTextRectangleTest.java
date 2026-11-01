package com.threeamigos.foresta.ui.testo;

import org.junit.jupiter.api.Test;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I rettangoli di testo scorrevole che prendono i glifi dal font a immagine: le righe sono alte quanto il font
 * (accenti e cediglie compresi), il raster è bianco e grigio su trasparente e il testo si legge nei punti giusti.
 */
class DoomdarkTextRectangleTest {

    private static int[] pixel(MemoryImageSource sorgente, int larghezza, int altezza) throws InterruptedException {
        Image immagine = Toolkit.getDefaultToolkit().createImage(sorgente);
        int[] pixel = new int[larghezza * altezza];
        PixelGrabber grabber = new PixelGrabber(immagine, 0, 0, larghezza, altezza, pixel, 0, larghezza);
        assertTrue(grabber.grabPixels(), "pixel non letti");
        return pixel;
    }

    /** Le righe del raster con almeno un pixel acceso */
    private static boolean rigaAccesa(int[] pixel, int larghezza, int riga) {
        for (int x = 0; x < larghezza; x++) {
            if ((pixel[riga * larghezza + x] >>> 24) != 0) {
                return true;
            }
        }
        return false;
    }

    @Test
    void ilRettangoloScorrevoleScriveLUltimaRigaInFondoEUsaLAltezzaPiena() throws InterruptedException {
        // Given
        int larghezza = 300;
        int altezza = 200;
        DoomdarkTextRectangle2x rettangolo = new DoomdarkTextRectangle2x(larghezza, altezza);
        DoomdarkFont font = DoomdarkFont.medio();
        // When
        rettangolo.addString("Foresta");
        int[] pixel = pixel(rettangolo.getImageSource(), larghezza, altezza);
        // Then: la riga è l'ultima, alta come il font; il corpo del testo sta fra le righe in più, le altre sono vuote
        int cima = altezza - font.getHeight();
        for (int riga = 0; riga < cima; riga++) {
            assertFalse(rigaAccesa(pixel, larghezza, riga), "riga " + riga + " sopra l'ultima non vuota");
        }
        assertFalse(rigaAccesa(pixel, larghezza, cima), "la prima riga in più, per gli accenti, è vuota per 'Foresta'");
        assertTrue(rigaAccesa(pixel, larghezza, cima + font.getRigheInCima()), "la prima riga del corpo ha la F");
        assertFalse(rigaAccesa(pixel, larghezza, altezza - 1), "le righe in fondo, per le cediglie, sono vuote");
        // I pixel accesi sono bianchi o grigi, mai colorati
        for (int p : pixel) {
            if ((p >>> 24) != 0) {
                assertEquals((p >> 16) & 0xFF, (p >> 8) & 0xFF);
                assertEquals((p >> 8) & 0xFF, p & 0xFF);
            }
        }
    }

    @Test
    void leRigheSonoSeparateDaUnPixelSolo() {
        // Altezza della riga di testo più un pixel: nella riga di 24 pixel ci sono già gli accenti e le cediglie
        int altezza = 200;
        DoomdarkTextRectangle2x rettangolo = new DoomdarkTextRectangle2x(300, altezza);
        int passo = DoomdarkFont.medio().getHeight() + 1;
        assertEquals((altezza - DoomdarkFont.medio().getHeight()) / passo + 1, rettangolo.getRigheVisibili());
    }

    @Test
    void unaFraseLungaVaACapoEUnMessaggioNuovoPortaInFondo() {
        DoomdarkTextRectangle2x rettangolo = new DoomdarkTextRectangle2x(120, 120);
        int versione = rettangolo.getVersione();
        for (int i = 0; i < 12; i++) {
            rettangolo.addString("Una frase abbastanza lunga da andare a capo più di una volta " + i);
        }
        assertTrue(rettangolo.getVersione() > versione);
        assertFalse(rettangolo.isScorribileGiu());
        assertTrue(rettangolo.isScorribileSu(), "c'è del testo più vecchio da rileggere");
        rettangolo.scorri(3);
        assertTrue(rettangolo.isScorribileGiu());
        rettangolo.addString("Nuovo");
        assertFalse(rettangolo.isScorribileGiu(), "un messaggio nuovo riporta in fondo");
        rettangolo.clear();
        assertFalse(rettangolo.isScorribileSu(), "svuotato, resta solo il cuscino di righe vuote ma non c'è altro da rileggere");
    }

    @Test
    void unCarattereCheNonCeNonDaErroriNeColonne() throws InterruptedException {
        DoomdarkTextRectangle2x rettangolo = new DoomdarkTextRectangle2x(200, 100);
        // La "_" non c'è nel font: si salta
        rettangolo.addString("Zo_");
        int[] con = pixel(rettangolo.getImageSource(), 200, 100);
        DoomdarkTextRectangle2x senza = new DoomdarkTextRectangle2x(200, 100);
        senza.addString("Zo");
        assertArrayEquals(pixel(senza.getImageSource(), 200, 100), con);
    }

    @Test
    void ilRettangoloPiccoloScorreVersoLAltoUnaRigaAllaVolta() throws InterruptedException {
        int larghezza = 200;
        int altezza = 80;
        DoomdarkTextRectangle rettangolo = new DoomdarkTextRectangle(larghezza, altezza);
        DoomdarkFont font = DoomdarkFont.piccolo();
        rettangolo.addString("Prima");
        int[] pixel = pixel(rettangolo.getImageSource(), larghezza, altezza);
        assertTrue(rigaAccesa(pixel, larghezza, altezza - font.getHeight() + font.getRigheInCima()), "'Prima' sta in fondo");
        // La seconda riga spinge la prima in alto di una riga di testo più un pixel
        rettangolo.addString("Seconda");
        pixel = pixel(rettangolo.getImageSource(), larghezza, altezza);
        assertTrue(rigaAccesa(pixel, larghezza, altezza - font.getHeight() + font.getRigheInCima()));
        assertTrue(rigaAccesa(pixel, larghezza, altezza - 2 * font.getHeight() - 1 + font.getRigheInCima()));
        rettangolo.clear();
        pixel = pixel(rettangolo.getImageSource(), larghezza, altezza);
        for (int riga = 0; riga < altezza; riga++) {
            assertFalse(rigaAccesa(pixel, larghezza, riga));
        }
    }
}
