package com.threeamigos.foresta.mappa;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import com.threeamigos.foresta.tipi.Quadrante;
import com.threeamigos.foresta.modellodati.ForestaMD;
import com.threeamigos.foresta.strumenti.dado.Dado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I quattro quadranti della Foresta: dove cominciano e finiscono, a quale appartiene una casella, e l'ordine casuale in cui si pescano.
 */
class QuadranteTest {

    @BeforeEach
    void preparaLaMappa() {
        ForestaMD dati = new ForestaMD();
        dati.reimposta(21, 15);
        Foresta.setFornitoreDiDati(() -> dati);
    }

    @AfterEach
    void ripristina() {
        Dado.ripristina();
    }

    @Test
    void conUnaDimensioneDispariLaMetaInPiuVaASudEdEst() {
        assertEquals(0, Quadrante.NORD_OVEST.getMinX());
        assertEquals(10, Quadrante.NORD_OVEST.getMaxX());
        assertEquals(10, Quadrante.NORD_EST.getMinX());
        assertEquals(21, Quadrante.NORD_EST.getMaxX());
        assertEquals(0, Quadrante.NORD_OVEST.getMinY());
        assertEquals(7, Quadrante.NORD_OVEST.getMaxY());
        assertEquals(7, Quadrante.SUD_OVEST.getMinY());
        assertEquals(15, Quadrante.SUD_EST.getMaxY());
    }

    @Test
    void ogniCasellaStaInUnSoloQuadrante() {
        for (int x = 0; x < 21; x++) {
            for (int y = 0; y < 15; y++) {
                CoordinateMD coordinate = new CoordinateMD(x, y);
                Quadrante quadrante = Quadrante.di(coordinate);
                assertTrue(quadrante.contiene(coordinate));
                for (Quadrante altro : Quadrante.values()) {
                    if (altro != quadrante) {
                        assertFalse(altro.contiene(coordinate));
                    }
                }
            }
        }
        assertThrows(IllegalArgumentException.class, () -> Quadrante.di(new CoordinateMD(21, 0)));
        assertThrows(IllegalArgumentException.class, () -> Quadrante.di(new CoordinateMD(0, -1)));
    }

    @Test
    void unaCasellaACasoDelQuadranteStaNelQuadrante() {
        Dado.impostaSeme(3);
        for (Quadrante quadrante : Quadrante.values()) {
            for (int i = 0; i < 50; i++) {
                assertTrue(quadrante.contiene(quadrante.getCoordinateACaso()));
            }
        }
    }

    @Test
    void iQuadrantiInOrdineCasualeSonoSempreTuttiEQuattro() {
        Dado.impostaSeme(5);
        for (int i = 0; i < 20; i++) {
            List<Quadrante> ordine = Quadrante.inOrdineCasuale();
            assertEquals(4, ordine.size());
            assertEquals(EnumSet.allOf(Quadrante.class), EnumSet.copyOf(ordine));
        }
    }
}
