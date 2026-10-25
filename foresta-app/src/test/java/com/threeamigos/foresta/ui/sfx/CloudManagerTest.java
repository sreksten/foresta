package com.threeamigos.foresta.ui.sfx;

import com.threeamigos.foresta.mappa.VistaMappa;
import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.TipoLocazione;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le nuvole della mappa si generano anche su un'area conosciuta stretta (un corridoio) e si dimenticano quando comincia un'altra partita.
 */
class CloudManagerTest {

    /** Una foresta di venti per venti caselle di cui se ne conosce solo un corridoio, alto una sola riga */
    private static VistaMappa corridoio(int caselle) {
        return foresta(20, 20, caselle);
    }

    private static VistaMappa foresta(int dimensioneX, int dimensioneY, int caselleConosciute) {
        return new VistaMappa() {
            @Override public int getDimensioneX() { return dimensioneX; }
            @Override public int getDimensioneY() { return dimensioneY; }
            @Override public TipoLocazione getLocazione(CoordinateMD coordinate) { return TipoLocazione.BOSCO; }
            @Override public int getVarianteBosco(CoordinateMD coordinate) { return 0; }
            @Override public boolean isLocazioneConosciuta(CoordinateMD coordinate) { return true; }
            @Override public boolean isLocazioneVisitata(CoordinateMD coordinate) { return false; }
            @Override public List<CoordinateMD> getCoordinateDaSegnalare() { return Collections.emptyList(); }
            @Override public int getVersioneMappa() { return 0; }
            @Override public int getMinXConosciuta() { return 0; }
            @Override public int getMaxXConosciuta() { return caselleConosciute - 1; }
            @Override public int getMinYConosciuta() { return 3; }
            @Override public int getMaxYConosciuta() { return 3; }
            @Override public String getNomeDaMostrare(CoordinateMD coordinate) { return null; }
            @Override public List<String> getNomiMissioniDaMostrare(CoordinateMD coordinate) { return Collections.emptyList(); }
        };
    }

    @AfterEach
    void azzera() {
        CloudManager.azzera();
    }

    // La foresta (20 x 20 caselle da 8 pixel) disegnata a (120, 20): al centro dello schermo di prova
    private static BufferedImage disegna() {
        BufferedImage schermo = new BufferedImage(400, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = schermo.createGraphics();
        try {
            CloudManager.disegna(g, 0, 0, 120, 20, 8, 8);
        } finally {
            g.dispose();
        }
        return schermo;
    }

    private static int pixelDisegnati() {
        return pixelDisegnati(0, 0, 400, 200);
    }

    private static int pixelDisegnati(int daX, int daY, int aX, int aY) {
        BufferedImage schermo = disegna();
        int pixel = 0;
        for (int x = daX; x < aX; x++) {
            for (int y = daY; y < aY; y++) {
                if ((schermo.getRGB(x, y) >>> 24) != 0) {
                    pixel++;
                }
            }
        }
        return pixel;
    }

    @Test
    void conUnCorridoioConosciutoLeNuvoleSiGenerano() {
        CloudManager.azzera();

        assertDoesNotThrow(() -> CloudManager.assicuraGenerate(corridoio(40), 400, 200, 8, 8));

        assertTrue(pixelDisegnati() > 0, "le nuvole ci sono e si disegnano");
    }

    @Test
    void conUnaCasellaSolaLeNuvoleSiGenerano() {
        CloudManager.azzera();

        assertDoesNotThrow(() -> CloudManager.assicuraGenerate(corridoio(1), 400, 200, 8, 8));
    }

    @Test
    void azzerandoLeNuvoleSparisconoFinoAllaProssimaGenerazione() {
        CloudManager.assicuraGenerate(corridoio(40), 400, 200, 8, 8);
        assertTrue(pixelDisegnati() > 0);

        CloudManager.azzera();

        assertEquals(0, pixelDisegnati(), "dopo l'azzeramento non ce n'è nessuna");
        CloudManager.assicuraGenerate(corridoio(40), 400, 200, 8, 8);
        assertTrue(pixelDisegnati() > 0, "e si rigenerano sulla nuova area");
    }

    @Test
    void leNuvoleVaganoSuTuttaLaForestaNonSoloSuQuellaConosciuta() {
        // Si conoscono solo le prime quattro caselle della riga 3: la foresta è di venti per venti, a partire da (120, 20)
        CloudManager.assicuraGenerate(foresta(20, 20, 4), 400, 200, 8, 8);

        int oltreLaZonaConosciuta = pixelDisegnati(120 + 4 * 8 + 30, 20, 400, 200);

        assertTrue(oltreLaZonaConosciuta > 0, "ci sono nuvole anche sulle caselle che non si conoscono");
    }
}
