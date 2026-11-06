package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.TipoIcona;
import com.threeamigos.foresta.ui.immagini.MisureDaiFile;
import com.threeamigos.foresta.ui.immagini.BufferedImageBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Le misure lette dai file senza caricarli (dai descrittori degli atlanti o dalle intestazioni dei PNG) sono quelle delle immagini
 * caricate.
 */
class MisureDaiFileTest {

    private static void stesseMisure(String risorsa) {
        Misura senzaToolkit = new MisureDaiFile().immagine(risorsa);
        java.awt.image.BufferedImage caricata = BufferedImageBuilder.buildBufferedImage(risorsa);
        assertEquals(caricata.getWidth(), senzaToolkit.larghezza, risorsa);
        assertEquals(caricata.getHeight(), senzaToolkit.altezza, risorsa);
    }

    @Test
    void leCorniciEGliSfondiDelleIcone() {
        stesseMisure(DisposizioneDelloSchermo.RISORSA_CORNICE_GRANDE);
        stesseMisure(DisposizioneDelloSchermo.RISORSA_CORNICE_MAPPA);
        stesseMisure(DisposizioneDelloSchermo.RISORSA_CORNICE_PICCOLA);
        stesseMisure(DisposizioneDelloSchermo.RISORSA_CORNICE_INCANTESIMI);
        stesseMisure(DisposizioneDelloSchermo.RISORSA_BOSCO);
        for (TipoIcona icona : TipoIcona.values()) {
            stesseMisure(icona.getSfondo().getRisorsa());
        }
    }
}
