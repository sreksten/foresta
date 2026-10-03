package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.OggettoLeggendario;

/**
 * Gli oggetti leggendari di leggendari.txt, per i test.
 */
final class Leggendari {

    private Leggendari() {
    }

    /**
     * Il leggendario con quel nome breve ("lo Scudo Fiscale").
     */
    static OggettoLeggendario con(String nomeBreve) {
        return ProduttoreDiTestiCasuale.tuttiGliOggettiLeggendari().stream().map(OggettoLeggendario::da)
                .filter(leggendario -> leggendario.getNomeBreve().equals(nomeBreve))
                .findFirst().orElseThrow(() -> new AssertionError("Leggendario non trovato: " + nomeBreve));
    }
}
