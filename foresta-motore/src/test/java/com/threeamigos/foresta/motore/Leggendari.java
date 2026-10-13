package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.OggettoLeggendario;

/**
 * Gli oggetti leggendari di leggendari.txt, per i test: si cercano per chiave, che non cambia, così i nomi e le
 * descrizioni si possono ritoccare senza toccare i test.
 */
final class Leggendari {

    /**
     * La Spada della Morte, che deve esserci sempre.
     */
    static final String SPADA_DI_ROMY_JONA = "SPADA_DI_ROMY_JONA";
    /**
     * Lo Scudo Fiscale, che deve esserci sempre.
     */
    static final String SCUDO_DELL_ESATTORE = "SCUDO_DELL_ESATTORE";

    private Leggendari() {
    }

    /**
     * Il leggendario con quella chiave.
     */
    static OggettoLeggendario con(String chiave) {
        return CatalogoLeggendari.getLeggendario(chiave).orElseThrow(() -> new AssertionError("Leggendario non trovato: " + chiave));
    }
}
