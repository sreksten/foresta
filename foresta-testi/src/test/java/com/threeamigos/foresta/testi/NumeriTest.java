package com.threeamigos.foresta.testi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * I numeri in lettere: i cardinali fino a dieci, gli ordinali fino a 41 e il loro articolo.
 */
class NumeriTest {

    @Test
    void iCardinaliFinoADieciSonoInLettereOltreRestanoInCifre() {
        assertEquals("zero", Numeri.getCardinaleM(0));
        assertEquals("uno", Numeri.getCardinaleM(1));
        assertEquals("dieci", Numeri.getCardinaleM(10));
        assertEquals("11", Numeri.getCardinaleM(11));
        assertEquals("-1", Numeri.getCardinaleM(-1));
    }

    @Test
    void alFemminileSoloUnoCambia() {
        assertEquals("una", Numeri.getCardinaleF(1));
        assertEquals("due", Numeri.getCardinaleF(2));
        assertEquals("12", Numeri.getCardinaleF(12));
    }

    @Test
    void gliOrdinaliHannoLaRadiceELaDesinenza() {
        assertEquals("primo", Numeri.getOrdinaleM(1));
        assertEquals("decimo", Numeri.getOrdinaleM(10));
        assertEquals("undicesimo", Numeri.getOrdinaleM(11));
        assertEquals("ventunesimo", Numeri.getOrdinaleM(21));
        assertEquals("quarantunesimo", Numeri.getOrdinaleM(41));
        assertEquals("prima", Numeri.getOrdinaleF(1));
        assertEquals("undicesima", Numeri.getOrdinaleF(11));
    }

    @Test
    void conLArticoloSiElideDavantiAOttavoEUndicesimo() {
        assertEquals("il primo", Numeri.getOrdinaleM(1, true));
        assertEquals("l'ottavo", Numeri.getOrdinaleM(8, true));
        assertEquals("l'undicesimo", Numeri.getOrdinaleM(11, true));
        assertEquals("la terza", Numeri.getOrdinaleF(3, true));
        assertEquals("l'ottava", Numeri.getOrdinaleF(8, true));
        assertEquals("primo", Numeri.getOrdinaleM(1, false), "senza articolo");
    }

    @Test
    void fuoriDaLimitiDegliOrdinaliNonCeUnRipiego() {
        assertEquals("nullo", Numeri.getOrdinaleM(0));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> Numeri.getOrdinaleM(42));
    }
}
