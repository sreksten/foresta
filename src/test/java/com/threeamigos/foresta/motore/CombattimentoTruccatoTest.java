package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.ModelloDati;
import com.threeamigos.foresta.motore.modellodati.SupertipoDanno;
import com.threeamigos.foresta.personaggi.Guerriero;
import com.threeamigos.foresta.personaggi.Ladro;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Esempio di dado truccato: il tiro per colpire (d100 contro una probabilità limitata tra 5 e 95) deciso dal test.
 */
class CombattimentoTruccatoTest {

    private Ladro attaccante;
    private Guerriero difensore;

    @BeforeEach
    void prepara() {
        ModelloDati.setIstanza(new ModelloDati());
        attaccante = new Ladro("Attaccante", 5);
        difensore = new Guerriero("Difensore", 5);
    }

    @AfterEach
    void ripristina() {
        assertEquals(0, Dado.trucchiRimasti(), "il test ha previsto più lanci di quelli fatti");
        Dado.ripristina();
    }

    @Test
    void unUnoColpisceSempre() {
        Dado.trucca(1);
        assertTrue(CalcolatoreCombattimento.colpisce(attaccante, difensore, SupertipoDanno.FISICO));
    }

    @Test
    void unCentoNonColpisceMai() {
        Dado.trucca(100);
        assertFalse(CalcolatoreCombattimento.colpisce(attaccante, difensore, SupertipoDanno.FISICO));
    }

    @Test
    void siColpisceFinoAllaProbabilitaCalcolata() {
        int probabilita = CalcolatoreCombattimento.calcolaProbabilitaDiColpire(attaccante, difensore, SupertipoDanno.FISICO);
        Dado.trucca(probabilita, probabilita + 1);
        assertTrue(CalcolatoreCombattimento.colpisce(attaccante, difensore, SupertipoDanno.FISICO));
        assertFalse(CalcolatoreCombattimento.colpisce(attaccante, difensore, SupertipoDanno.FISICO));
    }
}
