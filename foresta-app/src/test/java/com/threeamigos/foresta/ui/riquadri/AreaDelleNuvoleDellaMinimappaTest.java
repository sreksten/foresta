package com.threeamigos.foresta.ui.riquadri;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Le nuvole della minimappa si ritagliano nel quadrato della mappa, dentro la cornice del riquadro: x e y partono ciascuna dalla propria
 * coordinata dell'angolo, non dalla x per tutte e due.
 */
class AreaDelleNuvoleDellaMinimappaTest {

    @Test
    void ilQuadratoParteDalleDueCoordinateDellAngolo() {
        Rectangle area = DisplayableCanvasRiquadroMappa.areaDelleNuvole(100, 40, 224);

        assertEquals(new Rectangle(112, 52, 224, 224), area);
    }
}
