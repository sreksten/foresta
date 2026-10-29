package com.threeamigos.foresta.ui.tattiche;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Un menu a discesa senza scelte si disegna senza errori (a ogni fotogramma finché è aperto).
 */
class CasellaADiscesaVuotaTest {

    @Test
    void unMenuSenzaScelteSiDisegnaSenzaLanciare() {
        CasellaADiscesa<String> menu = new CasellaADiscesa<>(Collections.emptyList(), null,
                100, 100, 80, 640, 480, scelta -> { });
        BufferedImage schermo = new BufferedImage(640, 480, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = schermo.createGraphics();
        try {
            assertDoesNotThrow(() -> menu.disegna(graphics, 0, 0));
        } finally {
            graphics.dispose();
        }
    }
}
