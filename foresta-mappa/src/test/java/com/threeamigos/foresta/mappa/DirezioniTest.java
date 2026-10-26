package com.threeamigos.foresta.mappa;

import com.threeamigos.foresta.modellodati.CoordinateMD;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * La direzione e la distanza fra due caselle, a parole.
 */
class DirezioniTest {

    @Test
    void nelloStessoPuntoNonCeUnaDirezione() {
        assertEquals("proprio qui.", Direzioni.getDirezione(3, 4, 3, 4));
    }

    @Test
    void sottoUnGiornoSiContanoLeOre() {
        assertEquals("a un'ora di cammino verso est.", Direzioni.getDirezione(0, 0, 1, 0));
        assertEquals("a tre ore di cammino verso ovest.", Direzioni.getDirezione(0, 0, -3, 0));
        assertEquals("a un'ora di cammino verso sud.", Direzioni.getDirezione(5, 5, 5, 6));
        assertEquals("a un'ora di cammino verso nord.", Direzioni.getDirezione(5, 5, 5, 4));
    }

    @Test
    void daUnGiornoInPoiSiContanoIGiorni() {
        assertEquals("a un giorno di cammino verso est.", Direzioni.getDirezione(0, 0, 6, 0));
        assertEquals("a poco piu' di un giorno di cammino verso est.", Direzioni.getDirezione(0, 0, 8, 0));
        assertEquals("a due giorni di cammino verso est.", Direzioni.getDirezione(0, 0, 12, 0));
        assertEquals("a poco piu' di due giorni di cammino verso est.", Direzioni.getDirezione(0, 0, 14, 0));
    }

    @Test
    void conQuasiUnGiornoInPiuSiArrotondaPerEccesso() {
        assertEquals("a quasi due giorni di cammino verso est.", Direzioni.getDirezione(0, 0, 10, 0));
    }

    @Test
    void moltoLontanoSiDiceSoloLaDirezione() {
        assertEquals("lontano in direzione est.", Direzioni.getDirezione(0, 0, 40, 0));
        assertEquals("molto lontano in direzione est.", Direzioni.getDirezione(0, 0, 60, 0));
    }

    @Test
    void leDiagonaliHannoDueDirezioni() {
        assertEquals("a poco piu' di un giorno di cammino verso sudest.", Direzioni.getDirezione(0, 0, 4, 4));
        assertEquals("a poco piu' di un giorno di cammino verso sudovest.", Direzioni.getDirezione(4, 0, 0, 4));
        assertEquals("a poco piu' di un giorno di cammino verso nordest.", Direzioni.getDirezione(0, 4, 4, 0));
        assertEquals("a poco piu' di un giorno di cammino verso nordovest.", Direzioni.getDirezione(4, 4, 0, 0));
    }

    @Test
    void seUnAsseDominaSiDiceSoloQuello() {
        // Più in basso che di lato
        assertEquals("a poco piu' di un giorno di cammino verso sud.", Direzioni.getDirezione(0, 0, 1, 8));
        assertEquals("a poco piu' di un giorno di cammino verso nord.", Direzioni.getDirezione(0, 8, 1, 0));
        // Più di lato che in basso
        assertEquals("a poco piu' di un giorno di cammino verso est.", Direzioni.getDirezione(0, 0, 8, 1));
        assertEquals("a poco piu' di un giorno di cammino verso est.", Direzioni.getDirezione(0, 1, 8, 0));
        assertEquals("a poco piu' di un giorno di cammino verso ovest.", Direzioni.getDirezione(8, 0, 0, 1));
        assertEquals("a poco piu' di un giorno di cammino verso ovest.", Direzioni.getDirezione(8, 1, 0, 0));
    }

    @Test
    void siPuoDareAncheLaCoppiaDiCoordinate() {
        assertEquals(Direzioni.getDirezione(0, 0, 6, 0), Direzioni.getDirezione(new CoordinateMD(0, 0), new CoordinateMD(6, 0)));
    }
}
