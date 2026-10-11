package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.TipoDanno;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Il testo che dice a quale tipo di danno un personaggio è debole, resiste o è immune.
 */
class TestoReazioneDiClasseTest {

    @Test
    void ilTestoDiceAQualeDanno() {
        assertEquals("Debole a Sacro", TestoReazioneDiClasse.di(TipoDanno.SACRO, 2.0d));
        assertEquals("Resiste a Fuoco", TestoReazioneDiClasse.di(TipoDanno.FUOCO, 0.5d));
        assertEquals("Immune a Aria", TestoReazioneDiClasse.di(TipoDanno.ARIA, 0.0d));
    }

    @Test
    void ilColoreEVerdePerLaDebolezzaERossoPerLaResistenzaELImmunita() {
        assertEquals(DoomdarkColorModel.Color.GREEN, TestoReazioneDiClasse.colore(2.0d));
        assertEquals(DoomdarkColorModel.Color.RED, TestoReazioneDiClasse.colore(0.5d));
        assertEquals(DoomdarkColorModel.Color.RED, TestoReazioneDiClasse.colore(0.0d));
    }
}
