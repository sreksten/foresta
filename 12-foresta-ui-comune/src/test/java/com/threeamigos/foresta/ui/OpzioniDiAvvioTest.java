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
        assertNull(opzioni.getSchermoDiProva());
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

    @Test
    void loSchermoDiProvaSiDaPerProfiloOPerMisuraEPortaConSeLaBarraADestraEIlDito() {
        OpzioniDiAvvio profilo = OpzioniDiAvvio.leggi(new String[]{"schermo=Galaxy_S24"});
        assertEquals(com.threeamigos.foresta.tipi.SchermoDiProva.GALAXY_S24.getMisura(), profilo.getSchermoDiProva());
        assertEquals(Orientamento.VERTICALE, profilo.getOrientamento());
        assertTrue(profilo.isTocco());

        OpzioniDiAvvio misura = OpzioniDiAvvio.leggi(new String[]{"ORIZZONTALE", "SCHERMO=2400x1080@2.625"});
        assertEquals(2400, misura.getSchermoDiProva().getLarghezza());
        assertEquals(2.625, misura.getSchermoDiProva().getDensita());
        assertEquals(Orientamento.ORIZZONTALE, misura.getOrientamento(), "un orientamento scritto vince");

        OpzioniDiAvvio sbagliato = OpzioniDiAvvio.leggi(new String[]{"SCHERMO=enorme"});
        assertNull(sbagliato.getSchermoDiProva());
        assertEquals(Orientamento.ORIZZONTALE, sbagliato.getOrientamento());
        assertFalse(sbagliato.isTocco());
        assertTrue(sbagliato.getAltri().isEmpty(), "è ignorato, non passato a chi avvia");
    }
}
