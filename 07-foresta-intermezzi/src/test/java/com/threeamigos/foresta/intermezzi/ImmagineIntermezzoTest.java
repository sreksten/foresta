package com.threeamigos.foresta.intermezzi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Stefano Reksten
 */
class ImmagineIntermezzoTest {

    @Test
    void unoSpriteSheetScorreTuttiIFotogrammiInOrdine() {
        ImmagineIntermezzo sheet = ImmagineIntermezzo.spriteSheet("intermezzi/Prova.png", 3, 2, 4);

        assertEquals(0, sheet.getFotogrammaAl(0));
        assertEquals(1, sheet.getFotogrammaAl(0.25));
        assertEquals(5, sheet.getFotogrammaAl(1.25));
        // Dopo l'ultimo ricomincia dal primo
        assertEquals(0, sheet.getFotogrammaAl(1.5));
    }

    @Test
    void unaSequenzaSceglieIFotogrammiDaMostrare() {
        ImmagineIntermezzo sheet = ImmagineIntermezzo.spriteSheet("intermezzi/Prova.png", 4, 1, 2, 0, 1, 2, 1);

        assertEquals(0, sheet.getFotogrammaAl(0));
        assertEquals(2, sheet.getFotogrammaAl(1));
        assertEquals(1, sheet.getFotogrammaAl(1.5));
        assertEquals(0, sheet.getFotogrammaAl(2));
    }

    @Test
    void unaSequenzaNonPuoUscireDalloSheet() {
        assertThrows(IllegalArgumentException.class,
                () -> ImmagineIntermezzo.spriteSheet("intermezzi/Prova.png", 2, 2, 5, 0, 4));
    }
}
