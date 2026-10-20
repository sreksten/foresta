package com.threeamigos.foresta.ui.componenti;

import org.junit.jupiter.api.Test;

import java.awt.Image;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Un elenco senza righe, o con zero pixel di altezza a disposizione, non lancia a ogni fotogramma: produce un'immagine di almeno un pixel.
 */
class ComponenteScorrevoleVuotoTest {

    @Test
    void conAltezzaZeroONegativaSiProduceUnaImmagineValida() {
        ComponenteScorrevole<String> componente = new ComponenteScorrevole<>(120, 10, 2);

        for (int altezza : new int[]{0, -7}) {
            Image immagine = componente.produci(altezza, 0);
            assertNotNull(immagine, "altezza " + altezza);
            assertTrue(immagine.getHeight(null) >= 1, "altezza " + altezza);
            assertTrue(immagine.getWidth(null) >= 1, "altezza " + altezza);
        }
    }

    @Test
    void conLarghezzaZeroSiProduceUnaImmagineValida() {
        Image immagine = new ComponenteScorrevole<String>(0, 10, 2).produci(30, 0);

        assertTrue(immagine.getWidth(null) >= 1);
        assertTrue(immagine.getHeight(null) >= 1);
    }
}
