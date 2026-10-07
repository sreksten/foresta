package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.MossaCartaForbiciSasso;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiquadroSfidaTest {

    @Test
    void ogniMossaHaIlNomeDelSuoFileDiImmagine() {
        assertEquals("Carta", DisplayableCanvasRiquadroSfida.nomeDellaMano(MossaCartaForbiciSasso.CARTA));
        assertEquals("Forbici", DisplayableCanvasRiquadroSfida.nomeDellaMano(MossaCartaForbiciSasso.FORBICE));
        assertEquals("Sasso", DisplayableCanvasRiquadroSfida.nomeDellaMano(MossaCartaForbiciSasso.SASSO));
        for (MossaCartaForbiciSasso mossa : MossaCartaForbiciSasso.values()) {
            for (String lato : new String[]{"-sx.gif", "-dx.gif"}) {
                String risorsa = "/com/threeamigos/foresta/img/fondi/" + DisplayableCanvasRiquadroSfida.nomeDellaMano(mossa) + lato;
                assertTrue(DisplayableCanvasRiquadroSfida.class.getResource(risorsa) != null, "manca " + risorsa);
            }
        }
    }

    @Test
    void ilRiquadroNonSuperaMaiMetaSchermoENonIngrandisce() {
        // Due mani 680 x 680 affiancate in 640 x 480: conta la larghezza (320 / 1360)
        double scala = DisplayableCanvasRiquadroSfida.fattoreDiScala(640, 480, 1360, 680);
        assertEquals(320.0 / 1360, scala, 1e-9);
        assertTrue(1360 * scala <= 320 + 1e-9 && 680 * scala <= 240 + 1e-9);
        // Immagini alte e strette: conta l'altezza
        scala = DisplayableCanvasRiquadroSfida.fattoreDiScala(640, 480, 200, 1000);
        assertEquals(0.24, scala, 1e-9);
        assertTrue(200 * scala <= 320 && 1000 * scala <= 240 + 1e-9);
        // Immagini piccole: restano come sono
        assertEquals(1.0, DisplayableCanvasRiquadroSfida.fattoreDiScala(640, 480, 100, 50), 1e-9);
    }
}
