package com.threeamigos.foresta.ui.tutorial;

import com.threeamigos.foresta.ui.Area;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContornoDiRettangoliTest {

    /** I pixel coperti dai segmenti, per confrontare il contorno senza dipendere da come è spezzato */
    private static boolean[][] pixel(List<Area> segmenti, int larghezza, int altezza) {
        boolean[][] p = new boolean[altezza][larghezza];
        for (Area s : segmenti) {
            for (int y = s.getY(); y < s.getY() + s.getAltezza(); y++) {
                for (int x = s.getX(); x < s.getX() + s.getLarghezza(); x++) {
                    p[y][x] = true;
                }
            }
        }
        return p;
    }

    @Test
    void nienteRettangoliNienteContorno() {
        assertTrue(ContornoDiRettangoli.segmenti(Collections.<Area>emptyList(), 2, 2).isEmpty());
    }

    @Test
    void unRettangoloHaQuattroLatiSenzaBuchiAgliAngoli() {
        List<Area> segmenti = ContornoDiRettangoli.segmenti(Collections.singletonList(new Area(10, 10, 20, 10)), 0, 2);
        assertEquals(4, segmenti.size());
        boolean[][] p = pixel(segmenti, 40, 30);
        // L'anello centrato sul bordo: i quattro angoli ci sono, l'interno no
        assertTrue(p[9][9] && p[9][30] && p[20][9] && p[20][30]);
        assertFalse(p[15][20]);
        assertTrue(p[9][20] && p[20][20] && p[15][9] && p[15][30]);
    }

    @Test
    void ILatiSiAllargano() {
        List<Area> segmenti = ContornoDiRettangoli.segmenti(Collections.singletonList(new Area(10, 10, 20, 10)), 3, 2);
        boolean[][] p = pixel(segmenti, 50, 40);
        // Il bordo sta 3 pixel fuori dal rettangolo
        assertTrue(p[6][20]);
        assertFalse(p[10][20]);
    }

    @Test
    void IRettangoliContiguiHannoUnContornoSoloIntornoATutti() {
        // Due icone affiancate: nessuna linea nel mezzo
        List<Area> segmenti = ContornoDiRettangoli.segmenti(Arrays.asList(new Area(10, 10, 10, 10), new Area(20, 10, 10, 10)), 0, 2);
        boolean[][] p = pixel(segmenti, 40, 30);
        assertFalse(p[15][20], "fra le due icone non c'è un bordo");
        assertTrue(p[9][15] && p[9][25] && p[15][9] && p[15][30]);
    }

    @Test
    void ConIlDockLeIconeIngranditeFannoUnaLineaSpezzataAGradini() {
        // Un'icona grande in mezzo a due piccole, tutte con il fondo in basso (spostate di 5 pixel perché il bordo, centrato, non esca dal disegno)
        List<Area> aree = Arrays.asList(new Area(5, 15, 10, 10), new Area(15, 5, 10, 20), new Area(25, 15, 10, 10));
        List<Area> segmenti = ContornoDiRettangoli.segmenti(aree, 0, 2);
        boolean[][] p = pixel(segmenti, 50, 40);
        assertTrue(p[14][10], "bordo sopra la prima icona piccola");
        assertTrue(p[4][20], "bordo sopra quella grande, più in alto");
        assertFalse(p[14][20], "ma non a metà altezza sopra quella grande");
        assertTrue(p[10][14] || p[10][15], "il gradino sale lungo il fianco dell'icona grande");
        assertFalse(p[20][15], "e non c'è un bordo fra l'icona piccola e la grande");
    }

    @Test
    void IRettangoliStaccatiHannoUnBordoCiascuno() {
        List<Area> segmenti = ContornoDiRettangoli.segmenti(Arrays.asList(new Area(10, 10, 5, 5), new Area(30, 10, 5, 5)), 0, 2);
        assertEquals(8, segmenti.size());
    }
}
