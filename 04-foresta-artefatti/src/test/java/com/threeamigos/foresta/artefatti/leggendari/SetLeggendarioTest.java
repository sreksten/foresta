package com.threeamigos.foresta.artefatti.leggendari;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SetLeggendarioTest {

    private static void rifiuta(String riga, String messaggio) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> SetLeggendario.da(riga));
        assertTrue(e.getMessage().contains(messaggio), e.getMessage());
    }

    @Test
    void leggeUnaRiga() {
        SetLeggendario set = SetLeggendario.da("CHIAVE=GIOVENTU_RIBELLE;NOME=la Gioventù Ribelle;MOLTIPLICATORE=1.3;");
        assertEquals("GIOVENTU_RIBELLE", set.getChiave());
        assertEquals("la Gioventù Ribelle", set.getNome());
        assertEquals(1.3, set.getMoltiplicatore(), 0.0001);
    }

    @Test
    void unaRigaMalFattaSiRifiuta() {
        rifiuta("CHIAVE=A;NOME=un set;MOLTIPLICATORE=1.3;;senzauguale", "Un campo è CHIAVE=valore");
        rifiuta("CHIAVE=A;NOME=un set;MOLTIPLICATORE=1.3;BOH=1", "Campo sconosciuto: BOH");
        rifiuta("CHIAVE=A;CHIAVE=B;NOME=un set;MOLTIPLICATORE=1.3", "Campo ripetuto: CHIAVE");
        rifiuta("NOME=un set;MOLTIPLICATORE=1.3", "Manca il campo CHIAVE");
        rifiuta("CHIAVE=A;NOME=;MOLTIPLICATORE=1.3", "Manca il campo NOME");
        rifiuta("CHIAVE=A;NOME=un set;MOLTIPLICATORE=1", "più di 1");
    }

    @Test
    void unaRigaNullaSiRifiuta() {
        assertThrows(NullPointerException.class, () -> SetLeggendario.da(null));
    }
}
