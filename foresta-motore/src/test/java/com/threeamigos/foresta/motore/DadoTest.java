package com.threeamigos.foresta.motore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DadoTest {

    @AfterEach
    void ripristina() {
        Dado.ripristina();
        Dado.impostaSeme(System.nanoTime());
    }

    @Test
    void iLanciTruccatiEsconoInOrdinePoiTornaIlCaso() {
        Dado.trucca(6, 1, 0.25);
        assertEquals(6, Dado.tira(6));
        assertEquals(1, Dado.tira(1, 20));
        assertEquals(0.25, Dado.probabilita());
        assertEquals(0, Dado.trucchiRimasti());
        int lancio = Dado.tira(6);
        assertEquals(true, lancio >= 1 && lancio <= 6);
    }

    @Test
    void unTruccoFuoriIntervalloEUnErrore() {
        Dado.trucca(20);
        assertThrows(IllegalStateException.class, () -> Dado.tira(6));
    }

    @Test
    void unTruccoDelTipoSbagliatoEUnErrore() {
        Dado.trucca(0.5);
        assertThrows(IllegalStateException.class, () -> Dado.tira(6));
        Dado.trucca(3);
        assertThrows(IllegalStateException.class, Dado::probabilita);
        Dado.trucca(1.0);
        assertThrows(IllegalStateException.class, Dado::probabilita);
    }

    @Test
    void iLanciSenzaSceltaNonConsumanoTrucchi() {
        Dado.trucca(4);
        assertEquals(1, Dado.tiraAncheAUnaFaccia(1));
        assertEquals(7, Dado.tiraAncheSenzaRange(7, 7));
        assertEquals(1, Dado.trucchiRimasti());
        assertEquals(4, Dado.tiraAncheAUnaFaccia(6));
    }

    @Test
    void conLoStessoSemeLaSequenzaSiRipete() {
        assertEquals(sequenza(42), sequenza(42));
    }

    @Test
    void ilSemeValeAnchePerChiUsaLaSorgente() {
        Dado.impostaSeme(7);
        double primo = Dado.sorgente().nextDouble();
        Dado.impostaSeme(7);
        assertEquals(primo, Dado.sorgente().nextDouble());
    }

    private static List<Number> sequenza(long seme) {
        Dado.impostaSeme(seme);
        List<Number> lanci = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            lanci.add(Dado.tira(1, 100));
            lanci.add(Dado.probabilita());
        }
        return lanci;
    }
}
