package com.threeamigos.foresta.ui.testo;

import org.junit.jupiter.api.Test;

import java.awt.Image;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il produttore di testo che prende i glifi dall'immagine del font scrive come quello di sempre, tranne che nelle
 * larghezze dei glifi che l'immagine misura sui pixel e che i font di bit dichiarano diverse. Quando i glifi
 * dell'immagine saranno ritoccati (sfumature, glifi nuovi) i confronti pixel per pixel non varranno più e questo test va
 * ridotto ai controlli sulle larghezze e sulle altezze.
 */
class ImageBasedDoomdarkTextProducerTest {

    private static final DoomdarkFont[] FONT = {DoomdarkFontSmall.getInstance(), DoomdarkFontMedium.getInstance()};

    /** I caratteri che i due font hanno e che hanno la stessa larghezza dichiarata e misurata */
    private static String caratteriConLaStessaLarghezza(DoomdarkFont font) {
        ImageBasedDoomdarkFont aImmagine = ImageBasedDoomdarkFont.di(font);
        StringBuilder caratteri = new StringBuilder();
        for (String riga : CaratteriDelFont.RIGHE) {
            for (char c : riga.toCharArray()) {
                if (font.supportaIlCarattere(c) && aImmagine.supportaIlCarattere(c) && font.getGlyphWidth(c) == aImmagine.getGlyphWidth(c)) {
                    caratteri.append(c);
                }
            }
        }
        return caratteri.toString();
    }

    /**
     * Il testo nuovo è alto quanto la riga piena (corpo più righe in più per accenti e cediglie) per ogni riga di testo:
     * le righe del corpo sono quelle del testo di sempre, le altre, per i glifi che il font ha già, sono trasparenti.
     */
    private static void assertImmaginiUguali(DoomdarkFont font, Image atteso, Image trovato, String descrizione) {
        ImageBasedDoomdarkFont aImmagine = ImageBasedDoomdarkFont.di(font);
        BufferedImage a = (BufferedImage) atteso;
        BufferedImage b = (BufferedImage) trovato;
        int corpo = aImmagine.getAltezzaDelCorpo();
        int righeDiTesto = a.getHeight() / corpo;
        assertEquals(a.getWidth(), b.getWidth(), "larghezza " + descrizione);
        assertEquals(righeDiTesto * aImmagine.getHeight(), b.getHeight(), "altezza " + descrizione);
        for (int riga = 0; riga < righeDiTesto; riga++) {
            for (int y = 0; y < aImmagine.getHeight(); y++) {
                int yCorpo = y - aImmagine.getRigheInCima();
                for (int x = 0; x < a.getWidth(); x++) {
                    int atteso0 = yCorpo >= 0 && yCorpo < corpo ? a.getRGB(x, riga * corpo + yCorpo) : 0;
                    int trovato0 = b.getRGB(x, riga * aImmagine.getHeight() + y);
                    if (atteso0 >>> 24 == 0) {
                        assertEquals(0, trovato0 >>> 24, "pixel " + x + "," + y + " trasparente " + descrizione);
                    } else {
                        assertEquals(atteso0, trovato0, "pixel " + x + "," + y + " " + descrizione);
                    }
                }
            }
        }
    }

    @Test
    void conIGlifiCheNonCambianoDiLarghezzaScriveGliStessiPixelDelProduttoreDiSempre() {
        for (DoomdarkFont font : FONT) {
            String testo = caratteriConLaStessaLarghezza(font).replace(" ", "");
            assertTrue(testo.length() > 50, "pochi caratteri in comune: " + testo);
            for (DoomdarkColorModel.Color colore : DoomdarkColorModel.Color.values()) {
                assertImmaginiUguali(font, DoomdarkTextProducer.getImage(testo, font, colore),
                        ImageBasedDoomdarkTextProducer.getImage(testo, font, colore), font.getClass().getSimpleName() + " " + colore);
            }
        }
    }

    @Test
    void ilTestoASuPiuRigheSpezzaESCriveComeQuelloDiSempre() {
        for (DoomdarkFont font : FONT) {
            // Parole senza i glifi che cambiano di larghezza, con spazi (che non cambiano)
            String testo = "Foresta Antica Dei Cavalieri Che Giocano A Carte Forbici E Sasso 1234";
            for (int larghezzaMassima : new int[]{60, 100, 200}) {
                assertImmaginiUguali(font, DoomdarkTextProducer.getImage(testo, font, DoomdarkColorModel.Color.YELLOW, larghezzaMassima),
                        ImageBasedDoomdarkTextProducer.getImage(testo, font, DoomdarkColorModel.Color.YELLOW, larghezzaMassima),
                        font.getClass().getSimpleName() + " a " + larghezzaMassima);
            }
        }
    }

    @Test
    void unTestoVuotoEUnPixelLargoEAltoQuantoIlFont() {
        for (DoomdarkFont font : FONT) {
            BufferedImage immagine = (BufferedImage) ImageBasedDoomdarkTextProducer.getImage("", font);
            assertEquals(1, immagine.getWidth());
            assertEquals(ImageBasedDoomdarkFont.di(font).getHeight(), immagine.getHeight());
        }
    }

    @Test
    void unCarattereCheNonCeSiSaltaSenzaEccezioniEIlTestoRestaLargoComeSenza() {
        // La ñ è ancora un segnaposto nell'immagine
        for (DoomdarkFont font : FONT) {
            assertFalse(ImageBasedDoomdarkFont.di(font).supportaIlCarattere('ñ'));
            BufferedImage con = (BufferedImage) assertDoesNotThrow(() -> ImageBasedDoomdarkTextProducer.getImage("Zoñ", font));
            BufferedImage senza = (BufferedImage) ImageBasedDoomdarkTextProducer.getImage("Zo", font);
            assertEquals(senza.getWidth(), con.getWidth());
        }
    }

    @Test
    void laLarghezzaDelGlifoEQuellaMisurataSuiPixelNonQuellaDichiarata() {
        // La ì ha una colonna in meno nei pixel di quante ne dichiari il font di bit
        ImageBasedDoomdarkFont piccolo = ImageBasedDoomdarkFont.piccolo();
        assertTrue(piccolo.getGlyphWidth('ì') < DoomdarkFontSmall.getInstance().getGlyphWidth('ì'));
        // Lo spazio non ha pixel accesi: ha la larghezza del font di sempre
        assertEquals(DoomdarkFontSmall.getInstance().getGlyphWidth(' '), piccolo.getGlyphWidth(' '));
    }

    @Test
    void ilFontAImmagineHaLAltezzaELeMisureDelFontCorrispondente() {
        for (DoomdarkFont font : FONT) {
            ImageBasedDoomdarkFont aImmagine = ImageBasedDoomdarkFont.di(font);
            // Una riga di testo è alta quanto l'immagine intera: il corpo del font più le righe in più in cima e in fondo
            assertEquals(font.getHeight(), aImmagine.getAltezzaDelCorpo());
            assertEquals(aImmagine.getRigheInCima() + font.getHeight() + (font == FONT[0]
                    ? ImageBasedDoomdarkFont.EXTRA_IN_FONDO_PICCOLO : ImageBasedDoomdarkFont.EXTRA_IN_FONDO_MEDIO), aImmagine.getHeight());
            assertEquals(font == FONT[0] ? ImageBasedDoomdarkFont.EXTRA_IN_CIMA_PICCOLO : ImageBasedDoomdarkFont.EXTRA_IN_CIMA_MEDIO,
                    aImmagine.getRigheInCima());
            assertEquals(font.getSpacing(), aImmagine.getSpacing());
            assertEquals(font.getPadding(), aImmagine.getPadding());
            assertSame(aImmagine, ImageBasedDoomdarkFont.di(aImmagine));
            assertThrows(UnsupportedOperationException.class, () -> aImmagine.getGlyphData('A'));
            assertThrows(UnsupportedOperationException.class, aImmagine::getDataWidthInBytes);
        }
        assertSame(ImageBasedDoomdarkFont.piccolo(), ImageBasedDoomdarkFont.di(DoomdarkFontSmall.getInstance()));
        assertSame(ImageBasedDoomdarkFont.medio(), ImageBasedDoomdarkFont.di(DoomdarkFontMedium.getInstance()));
    }

    @Test
    void ilTestoSiScriveSoloConIFontDiSempreOQuelliAImmagine() {
        DoomdarkFont sconosciuto = new DoomdarkFont() {
            public int getHeight() { return 5; }
            public int getSpacing() { return 1; }
            public int getPadding() { return 1; }
            public int getDataWidthInBytes() { return 1; }
            public int getGlyphWidth(char c) { return 3; }
            public byte[] getGlyphData(char c) { return new byte[0]; }
        };
        assertThrows(IllegalArgumentException.class, () -> ImageBasedDoomdarkFont.di(sconosciuto));
    }

    @Test
    void ilLivelloELAlfaDelColoreEIlColoreResta() {
        int giallo = 0xFFFFFF00;
        // 0 trasparente, 255 il colore pieno
        assertEquals(0, ImageBasedDoomdarkTextProducer.pixel(giallo, 0));
        assertEquals(giallo, ImageBasedDoomdarkTextProducer.pixel(giallo, 255));
        // In mezzo il colore non cambia, cambia l'alfa: sfuma sullo sfondo
        assertEquals(0x7FFFFF00, ImageBasedDoomdarkTextProducer.pixel(giallo, 127));
        assertEquals(0x40FFFF00, ImageBasedDoomdarkTextProducer.pixel(giallo, 64));
        // Un colore già un po' trasparente: le due alfa si moltiplicano
        assertEquals(0x40FFFF00, ImageBasedDoomdarkTextProducer.pixel(0x80FFFF00, 128));
    }

    @Test
    void iPixelOpachiDelTestoSonoDelColoreDelTestoNonDiUnGrigio() {
        int colore = DoomdarkColorModel.getColorModel(DoomdarkColorModel.Color.YELLOW).getRGB(1);
        BufferedImage immagine = (BufferedImage) ImageBasedDoomdarkTextProducer.getImage("A", FONT[0], DoomdarkColorModel.Color.YELLOW);
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
