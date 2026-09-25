package com.threeamigos.foresta.ui.sfx;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TracciatoreLogoTest {

    private static BufferedImage logo3AM;

    @BeforeAll
    static void caricaLogo() throws IOException {
        logo3AM = ImageIO.read(TracciatoreLogoTest.class.getResource("/com/threeamigos/foresta/img/Logo3AM.png"));
    }

    @Test
    void nelLogo3AMTrovaTreLettereEIlBucoDellaA() {
        TracciatoreLogo effetto = TracciatoreLogo.costruttore(logo3AM).costruisci();
        assertTrue(effetto.getDiagnostica().get(0).startsWith("4 forma/e rilevate (3 contorni esterni, 1 buchi interni)"),
                effetto.getDiagnostica().get(0));
    }

    @Test
    void senzaRipetizioneFinisceDopoUnCiclo() {
        TracciatoreLogo effetto = TracciatoreLogo.costruttore(logo3AM).costruisci();
        int fotogrammi = 0;
        while (!effetto.isFinito()) {
            effetto.avanza();
            fotogrammi++;
            assertTrue(fotogrammi <= effetto.stimaFotogrammiCiclo(), "non finisce entro la stima");
        }
        assertEquals("PAUSA", effetto.getNomeFase());
        effetto.avanza();
        assertTrue(effetto.isFinito(), "finito resta finito");

        effetto.ricomincia();
        assertFalse(effetto.isFinito());
        assertEquals("TRACCIAMENTO", effetto.getNomeFase());
    }

    @Test
    void conLaRipetizioneNonFinisceMai() {
        TracciatoreLogo effetto = TracciatoreLogo.costruttore(logo3AM).ripeti(true).costruisci();
        for (int i = 0; i < 3 * effetto.stimaFotogrammiCiclo(); i++) {
            effetto.avanza();
            assertFalse(effetto.isFinito());
        }
    }

    @Test
    void siDisegnaSenzaSchermo() {
        TracciatoreLogo effetto = TracciatoreLogo.costruttore(logo3AM).coloreSfondo(Color.BLACK).costruisci();
        BufferedImage fotogramma = new BufferedImage(effetto.getLarghezza(), effetto.getAltezza(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = fotogramma.createGraphics();
        for (int i = 0; i < 20; i++) {
            effetto.avanza();
        }
        effetto.disegna(g2);
        g2.dispose();
        boolean sfondo = false, scia = false;
        for (int y = 0; y < fotogramma.getHeight(); y++) {
            for (int x = 0; x < fotogramma.getWidth(); x++) {
                int rgb = fotogramma.getRGB(x, y) & 0xFFFFFF;
                sfondo |= rgb == 0x000000;
                scia |= rgb == 0xFFFFFF;
            }
        }
        assertTrue(sfondo, "manca lo sfondo nero");
        assertTrue(scia, "manca la scia bianca");
    }

    @Test
    void unaRegioneFuoriDallImmagineERifiutata() {
        assertThrows(IllegalArgumentException.class,
                () -> TracciatoreLogo.costruttore(logo3AM).regione(new Rectangle(200, 0, 50, 50)).costruisci());
    }

    @Test
    void unImmagineVuotaNonHaContorni() {
        BufferedImage vuota = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        assertThrows(IllegalStateException.class, () -> TracciatoreLogo.costruttore(vuota).costruisci());
    }
}
