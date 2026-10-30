package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.ui.sprite.SpriteATempo;
import com.threeamigos.foresta.ui.sprite.SpriteEffetto;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Gli sprite di testo ("Mancato", gli effetti di stato) e le variazioni di salute e magia si centrano sul personaggio e si spostano solo se uscirebbero dal riquadro.
 */
class SpriteDiTestoCentratoTest {

    @Test
    void seCiStaRestaCentratoSulPersonaggio() {
        assertEquals(300.0f, SpriteEffetto.centroEntro(300.0f, 100.0f, 0.0f, 800.0f));
    }

    @Test
    void vicinoAlBordoSinistroSiSpostaVersoDentro() {
        assertEquals(50.0f, SpriteEffetto.centroEntro(10.0f, 100.0f, 0.0f, 800.0f));
    }

    @Test
    void vicinoAlBordoDestroSiSpostaVersoDentro() {
        assertEquals(750.0f, SpriteEffetto.centroEntro(790.0f, 100.0f, 0.0f, 800.0f));
    }

    @Test
    void seNonCiStaStaAlCentroDelRiquadro() {
        assertEquals(50.0f, SpriteEffetto.centroEntro(10.0f, 200.0f, 0.0f, 100.0f));
    }

    /**
     * Dove, a x, lo sprite ha disegnato qualcosa nella riga y: da quale colonna a quale colonna (esclusa), o null se niente.
     */
    private static int[] colonneDisegnate(SpriteATempo sprite, int larghezzaTela, int y) {
        BufferedImage tela = new BufferedImage(larghezzaTela, 60, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tela.createGraphics();
        sprite.anima(g);
        g.dispose();
        int primo = -1;
        int ultimo = -1;
        for (int x = 0; x < larghezzaTela; x++) {
            if ((tela.getRGB(x, y) >>> 24) != 0) {
                if (primo < 0) {
                    primo = x;
                }
                ultimo = x;
            }
        }
        return primo < 0 ? null : new int[] {primo, ultimo + 1};
    }

    private static BufferedImage iconaRossa(int larghezza, int altezza) {
        BufferedImage icona = new BufferedImage(larghezza, altezza, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icona.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, larghezza, altezza);
        g.dispose();
        return icona;
    }

    @Test
    void lVariazioneDiSaluteESiCentraSulPersonaggio() {
        SpriteATempo sprite = new SpriteATempo(iconaRossa(20, 10), 100, 20, "prova");
        sprite.centraEntro(100.0f, 0.0f, 200.0f);
        assertArrayEquals(new int[] {90, 110}, colonneDisegnate(sprite, 200, 25));
    }

    @Test
    void lVariazioneDiSaluteVicinoAlBordoRestaNelRiquadro() {
        SpriteATempo sprite = new SpriteATempo(iconaRossa(20, 10), 100, 20, "prova");
        sprite.centraEntro(3.0f, 0.0f, 200.0f);
        assertArrayEquals(new int[] {0, 20}, colonneDisegnate(sprite, 200, 25));
    }
}
