package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.tipi.Orientamento;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le opzioni di avvio comuni a Swing e libGDX.
 */
class OpzioniDiAvvioTest {

    @Test
    void senzaArgomentiValgonoIPredefiniti() {
        OpzioniDiAvvio opzioni = OpzioniDiAvvio.leggi(new String[0]);
        assertEquals(Orientamento.ORIZZONTALE, opzioni.getOrientamento());
        assertTrue(opzioni.isBarraDock());
        assertFalse(opzioni.isTuttoSchermo());
        assertFalse(opzioni.isSaltaLogo());
        assertFalse(opzioni.isJoystickVirtuale());
        assertFalse(opzioni.isTocco());
        assertFalse(opzioni.isLinguaDecisa());
        assertTrue(opzioni.getAltri().isEmpty());
    }

    @Test
    void leOpzioniSiLeggonoSenzaBadareAlleMaiuscoleEGliAltriArgomentiRestano() {
        OpzioniDiAvvio opzioni = OpzioniDiAvvio.leggi(new String[]{"verticale", "TuttoSchermo", "SALTALOGO", "barraclassica",
                "joystick_virtuale", "Tocco", "Pagine_Di_Prova", "boh"});
        assertEquals(Orientamento.VERTICALE, opzioni.getOrientamento());
        assertTrue(opzioni.isTuttoSchermo());
        assertTrue(opzioni.isSaltaLogo());
        assertFalse(opzioni.isBarraDock());
        assertTrue(opzioni.isJoystickVirtuale());
        assertTrue(opzioni.isTocco());
        assertEquals(Arrays.asList("Pagine_Di_Prova", "boh"), opzioni.getAltri());
        assertTrue(opzioni.haAltro("PAGINE_DI_PROVA"));
        assertFalse(opzioni.haAltro("MODALITA_DI_PROVA"));
    }
}
