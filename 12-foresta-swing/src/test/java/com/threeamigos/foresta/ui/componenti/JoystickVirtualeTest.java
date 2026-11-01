package com.threeamigos.foresta.ui.componenti;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JoystickVirtualeTest {

    private final JoystickVirtuale joystick = new JoystickVirtuale(1000, 800);
    private final int cx = joystick.getCentroX();
    private final int cy = joystick.getCentroY();

    @Test
    void ilDiscoStaInBassoADestra() {
        assertTrue(cx > 500 && cy > 400);
        assertTrue(cx + JoystickVirtuale.RAGGIO <= 1000 && cy + JoystickVirtuale.RAGGIO <= 800);
    }

    @Test
    void siAttivaSoloPremendoSulDisco() {
        assertFalse(joystick.attiva(cx - JoystickVirtuale.RAGGIO - 1, cy));
        assertFalse(joystick.isAttivo());
        assertTrue(joystick.attiva(cx + JoystickVirtuale.RAGGIO - 1, cy));
        assertTrue(joystick.isAttivo());
    }

    @Test
    void nonAttivoNonMuove() {
        joystick.sposta(cx + JoystickVirtuale.RAGGIO, cy);
        assertEquals(0, joystick.getIntensita());
        assertEquals(0, joystick.getVelocitaX());
        assertEquals(0, joystick.getVelocitaY());
    }

    @Test
    void nellaZonaCentraleNonMuove() {
        joystick.attiva(cx, cy);
        joystick.sposta(cx + JoystickVirtuale.RAGGIO_ZONA_CENTRALE, cy);
        assertEquals(0, joystick.getVelocitaX());
    }

    @Test
    void laVelocitaCresceConLaDistanzaDallaZonaCentrale() {
        joystick.attiva(cx, cy);
        int meta = (JoystickVirtuale.RAGGIO_ZONA_CENTRALE + JoystickVirtuale.RAGGIO) / 2;
        joystick.sposta(cx + meta, cy);
        assertEquals(0.5, joystick.getIntensita(), 0.02);
        assertEquals(JoystickVirtuale.VELOCITA_MASSIMA / 2, joystick.getVelocitaX(), JoystickVirtuale.VELOCITA_MASSIMA * 0.02);
        assertEquals(0, joystick.getVelocitaY(), 1e-9);
    }

    @Test
    void oltreIlBordoLaVelocitaNonCresce() {
        joystick.attiva(cx, cy);
        joystick.sposta(cx, cy + 5 * JoystickVirtuale.RAGGIO);
        assertEquals(1, joystick.getIntensita(), 1e-9);
        assertEquals(JoystickVirtuale.VELOCITA_MASSIMA, joystick.getVelocitaY(), 1e-9);
        assertEquals(0, joystick.getVelocitaX(), 1e-9);
    }

    @Test
    void laDirezioneEQuellaDelPuntatore() {
        joystick.attiva(cx, cy);
        joystick.sposta(cx - JoystickVirtuale.RAGGIO, cy - JoystickVirtuale.RAGGIO);
        assertTrue(joystick.getVelocitaX() < 0 && joystick.getVelocitaY() < 0);
        assertEquals(JoystickVirtuale.VELOCITA_MASSIMA, Math.hypot(joystick.getVelocitaX(), joystick.getVelocitaY()), 1e-6);
    }

    @Test
    void rilasciatoSiFerma() {
        joystick.attiva(cx, cy);
        joystick.sposta(cx + JoystickVirtuale.RAGGIO, cy);
        joystick.rilascia();
        assertFalse(joystick.isAttivo());
        assertEquals(0, joystick.getVelocitaX());
    }
}
