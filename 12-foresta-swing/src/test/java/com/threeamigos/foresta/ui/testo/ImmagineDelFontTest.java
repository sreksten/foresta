package com.threeamigos.foresta.ui.testo;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La lettura dei font Doomdark scritti in un'immagine: la griglia si calcola dalle dimensioni, un riquadro con un pixel
 * rosso è un glifo che manca, il nero puro è trasparente e la larghezza di un glifo è il suo pixel acceso più a destra.
 */
class ImmagineDelFontTest {

    private static final int ROSSO = 0xFF0000;
    private static final int LARGHEZZA_RIQUADRO = 4;
    private static final int ALTEZZA_RIQUADRO = 5;
    private static final int PASSO_X = LARGHEZZA_RIQUADRO + 1;
    private static final int PASSO_Y = ALTEZZA_RIQUADRO + 1;
    private static final int LARGHEZZA_DELLO_SPAZIO = 3;

    /** Un'immagine di tutti riquadri neri con la griglia rossa: la prima riga e colonna, e l'ultima di ogni riquadro */
    private static BufferedImage griglia() {
        int colonne = CaratteriDelFont.colonne();
        int righe = CaratteriDelFont.RIGHE.length;
        BufferedImage immagine = new BufferedImage(1 + colonne * PASSO_X, 1 + righe * PASSO_Y, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < immagine.getWidth(); x++) {
            for (int y = 0; y < immagine.getHeight(); y++) {
                if (x % PASSO_X == 0 || y % PASSO_Y == 0) {
                    immagine.setRGB(x, y, ROSSO);
                }
            }
        }
        return immagine;
    }

    /** Il pixel (x, y) del riquadro in quella riga e colonna */
    private static void punto(BufferedImage immagine, int riga, int colonna, int x, int y, int rgb) {
        immagine.setRGB(1 + colonna * PASSO_X + x, 1 + riga * PASSO_Y + y, rgb);
    }

    private static int grigio(int livello) {
        return (livello << 16) | (livello << 8) | livello;
    }

    @Test
    void ilGlifoHaLaLarghezzaDelSuoPixelPiuADestraETiene255LivelliDiGrigio() {
        // Given: la A in prima riga e prima colonna, con un pixel bianco e uno grigio più a destra
        BufferedImage immagine = griglia();
        punto(immagine, 0, 0, 0, 0, grigio(255));
        punto(immagine, 0, 0, 2, 1, grigio(128));
        // When
        ImmagineDelFont font = daImmagine(immagine, LARGHEZZA_DELLO_SPAZIO);
        // Then
        GlifoDelFont a = font.getGlifo('A');
        assertEquals(3, a.getLarghezza());
        assertEquals(ALTEZZA_RIQUADRO, a.getAltezza());
        assertEquals(ALTEZZA_RIQUADRO, font.getAltezza());
        assertEquals(255, a.getLivello(0, 0));
        assertEquals(128, a.getLivello(2, 1));
        assertEquals(0, a.getLivello(1, 0), "il nero puro è trasparente");
    }

    @Test
    void conUnCanaleAlfaIlLivelloEIlGrigioPerLAlfaEUnPixelTrasparenteNonConta() {
        // Given: un PNG, cioè con canale alfa, con lo sfondo trasparente: un pixel bianco pieno, uno bianco a metà
        // trasparenza e uno trasparente con un colore qualunque (che non conta)
        int colonne = CaratteriDelFont.colonne();
        int righe = CaratteriDelFont.RIGHE.length;
        BufferedImage immagine = new BufferedImage(1 + colonne * PASSO_X, 1 + righe * PASSO_Y, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < immagine.getWidth(); x++) {
            for (int y = 0; y < immagine.getHeight(); y++) {
                if (x % PASSO_X == 0 || y % PASSO_Y == 0) {
                    immagine.setRGB(x, y, 0xFF000000 | ROSSO);
                }
            }
        }
        immagine.setRGB(1, 1, 0xFFFFFFFF);
        immagine.setRGB(2, 1, 0x80FFFFFF);
        immagine.setRGB(3, 1, 0x000000FF);
        // When
        GlifoDelFont a = daImmagine(immagine, LARGHEZZA_DELLO_SPAZIO).getGlifo('A');
        // Then
        assertEquals(255, a.getLivello(0, 0));
        assertEquals(128, a.getLivello(1, 0));
        assertEquals(0, a.getLivello(2, 0));
        assertEquals(2, a.getLarghezza(), "il pixel trasparente non conta per la larghezza");
    }

    @Test
    void unRiquadroConUnPixelRossoEUnGlifoCheManca() {
        // Given: la B è un segnaposto, in parte disegnato
        BufferedImage immagine = griglia();
        punto(immagine, 0, 1, 0, 0, grigio(255));
        punto(immagine, 0, 1, 1, 1, ROSSO);
        // When
        ImmagineDelFont font = daImmagine(immagine, LARGHEZZA_DELLO_SPAZIO);
        // Then
        assertFalse(font.supportaIlCarattere('B'));
        assertTrue(font.getCaratteriMancanti().contains('B'));
        DoomdarkFont.UnsupportedCharacterException eccezione =
                assertThrows(DoomdarkFont.UnsupportedCharacterException.class, () -> font.getGlifo('B'));
        assertTrue(eccezione.getMessage().contains("B"));
    }

    @Test
    void unRiquadroTuttoNeroENonSpazioEUnGlifoCheManca() {
        ImmagineDelFont font = daImmagine(griglia(), LARGHEZZA_DELLO_SPAZIO);
        assertFalse(font.supportaIlCarattere('a'));
        assertTrue(font.getCaratteriMancanti().contains('a'));
    }

    @Test
    void loSpazioHaLaLarghezzaDataENessunPixelAcceso() {
        // Given: lo spazio nella riga dei segni è un riquadro tutto nero
        BufferedImage immagine = griglia();
        // When
        GlifoDelFont spazio = daImmagine(immagine, LARGHEZZA_DELLO_SPAZIO).getGlifo(' ');
        // Then
        assertEquals(LARGHEZZA_DELLO_SPAZIO, spazio.getLarghezza());
        assertEquals(ALTEZZA_RIQUADRO, spazio.getAltezza());
        for (int y = 0; y < spazio.getAltezza(); y++) {
            for (int x = 0; x < spazio.getLarghezza(); x++) {
                assertEquals(0, spazio.getLivello(x, y));
            }
        }
    }

    @Test
    void unColoreCheNonEGrigioNeRossoEUnErroreDellImmagine() {
        BufferedImage immagine = griglia();
        punto(immagine, 0, 2, 1, 1, 0x0000FF);
        IllegalStateException eccezione = assertThrows(IllegalStateException.class,
                () -> daImmagine(immagine, LARGHEZZA_DELLO_SPAZIO));
        assertTrue(eccezione.getMessage().contains("'C'"), eccezione.getMessage());
    }

    @Test
    void unaImmagineCheNonEUnaGrigliaDiColonneERigheEUnErrore() {
        BufferedImage storta = new BufferedImage(1 + CaratteriDelFont.colonne() * PASSO_X + 1, 1 + 3 * PASSO_Y, BufferedImage.TYPE_INT_RGB);
        assertThrows(IllegalStateException.class, () -> daImmagine(storta, LARGHEZZA_DELLO_SPAZIO));
    }

    @Test
    void gliElenchiNonHannoCaratteriRipetuti() {
        for (String riga : CaratteriDelFont.RIGHE) {
            for (int i = 0; i < riga.length(); i++) {
                assertEquals(i, riga.indexOf(riga.charAt(i)), "carattere ripetuto: '" + riga.charAt(i) + "'");
            }
        }
        // Nessuno dei tre elenchi ripete un carattere di un altro
        String tutti = String.join("", CaratteriDelFont.RIGHE);
        for (int i = 0; i < tutti.length(); i++) {
            assertEquals(i, tutti.indexOf(tutti.charAt(i)), "carattere in più righe: '" + tutti.charAt(i) + "'");
        }
    }

    @Test
    void leImmaginiDeiDueFontHannoTuttiICaratteriDellElenco() {
        for (DoomdarkFont font : new DoomdarkFont[]{DoomdarkFont.piccolo(), DoomdarkFont.medio()}) {
            for (String riga : CaratteriDelFont.RIGHE) {
                for (char c : riga.toCharArray()) {
                    assertTrue(font.supportaIlCarattere(c) || c == ' ' , "manca nell'immagine '" + c + "'");
                    assertTrue(font.getGlyphWidth(c) >= 1, "'" + c + "'");
                }
            }
        }
    }

    @Test
    void unaRisorsaCheNonEsisteDaUnErrore() {
        assertThrows(IllegalStateException.class, () -> ImmagineDelFont.carica("/non/esiste.png", 4));
    }

    private static ImmagineDelFont daImmagine(java.awt.image.BufferedImage immagine, int larghezzaDelloSpazio) {
        return ImmagineDelFont.daImmagine(com.threeamigos.foresta.ui.immagini.ImmaginiAwt.pixel(immagine), larghezzaDelloSpazio);
    }
}
