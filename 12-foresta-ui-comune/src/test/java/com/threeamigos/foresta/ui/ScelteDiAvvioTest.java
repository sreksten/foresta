package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.strumenti.ModalitaDiProva;
import com.threeamigos.foresta.tipi.Orientamento;
import com.threeamigos.foresta.tipi.SchermoDiProva;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Le opzioni di avvio della finestra delle opzioni: si leggono dagli argomenti senza impostare niente e si riscrivono come argomenti.
 */
class ScelteDiAvvioTest {

    @AfterEach
    void rimettiIPredefiniti() {
        ModalitaDiProva.setAttiva(false);
        LivelloDiZoom.imposta(LivelloDiZoom.PREDEFINITO);
        com.threeamigos.foresta.testi.Testi.impostaLingua(null);
    }

    @Test
    void laModalitaDiProvaSiRiconosceSenzaBadareAlleMaiuscole() {
        assertTrue(ScelteDiAvvio.isModalitaDiProva(new String[]{"saltalogo", "Modalita_Di_Prova"}));
        assertFalse(ScelteDiAvvio.isModalitaDiProva(new String[]{"SALTALOGO"}));
    }

    @Test
    void leggereNonImpostaNiente() {
        ScelteDiAvvio.da(new String[]{"MODALITA_DI_PROVA", "ZOOM=3", "LINGUA=en"});
        assertFalse(ModalitaDiProva.isAttiva());
        assertEquals(LivelloDiZoom.PREDEFINITO, LivelloDiZoom.valore());
    }

    @Test
    void gliArgomentiSiRileggonoUgualiEGliAltriRestano() {
        ScelteDiAvvio scelte = ScelteDiAvvio.da(new String[]{"modalita_di_prova", "verticale", "schermo=pixel_8", "LINGUA=en-GB", "ZOOM=1.5",
                "TUTTOSCHERMO", "SALTALOGO", "BARRACLASSICA", "JOYSTICK_VIRTUALE", "TOCCO", "boh"});
        assertTrue(scelte.isModalitaDiProva());
        assertEquals(SchermoDiProva.PIXEL_8, scelte.getProfilo());
        assertEquals(Orientamento.VERTICALE, scelte.getOrientamento());
        assertEquals("en-GB", scelte.getLingua());
        assertEquals(1.5, scelte.getZoom());
        assertEquals(Collections.singletonList("boh"), scelte.getAltri());
        assertEquals(Arrays.asList("MODALITA_DI_PROVA", "SCHERMO=PIXEL_8", "VERTICALE", "LINGUA=en-GB", "ZOOM=1.5", "TUTTOSCHERMO", "SALTALOGO",
                "BARRACLASSICA", "JOYSTICK_VIRTUALE", "TOCCO", "boh"), Arrays.asList(scelte.comeArgomenti()));

        OpzioniDiAvvio opzioni = OpzioniDiAvvio.leggi(scelte.comeArgomenti());
        assertTrue(ModalitaDiProva.isAttiva());
        assertEquals(SchermoDiProva.PIXEL_8.getMisura(), opzioni.getSchermoDiProva());
        assertEquals(Orientamento.VERTICALE, opzioni.getOrientamento());
        assertTrue(opzioni.isLinguaDecisa());
        assertEquals(1.5, LivelloDiZoom.valore());
        assertTrue(opzioni.isTuttoSchermo());
        assertTrue(opzioni.isSaltaLogo());
        assertFalse(opzioni.isBarraDock());
        assertTrue(opzioni.isJoystickVirtuale());
        assertEquals(Collections.singletonList("boh"), opzioni.getAltri());
    }

    @Test
    void senzaArgomentiNonSiScriveNiente() {
        assertEquals(0, ScelteDiAvvio.da(new String[0]).comeArgomenti().length);
    }

    @Test
    void unoSchermoAMisuraSiScriveConLaDensitaESostituisceIlProfilo() {
        ScelteDiAvvio scelte = ScelteDiAvvio.da(new String[]{"SCHERMO=1080x2400@2.625"});
        assertNull(scelte.getProfilo());
        assertEquals(2400, scelte.getSchermoAMisura().getLarghezza());
        assertEquals(Collections.singletonList("SCHERMO=2400x1080@2.625"), Arrays.asList(scelte.comeArgomenti()));
        scelte.impostaProfilo(SchermoDiProva.IPAD);
        assertNull(scelte.getSchermoAMisura());
        scelte.impostaSchermoAMisura(new MisuraDelloSchermo(800, 480, 1));
        assertNull(scelte.getProfilo());
        assertEquals(Collections.singletonList("SCHERMO=800x480@1"), Arrays.asList(scelte.comeArgomenti()));
    }

    @Test
    void spentaLaModalitaDiProvaNonSiScrive() {
        ScelteDiAvvio scelte = ScelteDiAvvio.da(new String[]{"MODALITA_DI_PROVA", "SALTALOGO"});
        scelte.setModalitaDiProva(false);
        assertEquals(Collections.singletonList("SALTALOGO"), Arrays.asList(scelte.comeArgomenti()));
    }

    @Test
    void zoomESchermoCheNonSiCapisconoRestanoFraGliAltri() {
        ScelteDiAvvio scelte = ScelteDiAvvio.da(new String[]{"ZOOM=tanto", "SCHERMO=enorme"});
        assertEquals(LivelloDiZoom.PREDEFINITO, scelte.getZoom());
        assertNull(scelte.getProfilo());
        assertNull(scelte.getSchermoAMisura());
        assertEquals(Arrays.asList("ZOOM=tanto", "SCHERMO=enorme"), scelte.getAltri());
    }

    @Test
    void laRigaDiComandoPortaLaCartellaDelLogComeProprieta() {
        ScelteDiAvvio scelte = ScelteDiAvvio.da(new String[]{"SALTALOGO"});
        scelte.setCartellaDiLog("/Volumes/ForestaLog");
        assertEquals("-Dforesta.log=/Volumes/ForestaLog SALTALOGO", scelte.comeRigaDiComando());
        scelte.setCartellaDiLog("  ");
        assertNull(scelte.getCartellaDiLog());
        assertEquals("SALTALOGO", scelte.comeRigaDiComando());
    }
}
