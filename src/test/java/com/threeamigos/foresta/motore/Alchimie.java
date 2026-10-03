package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.LAlchimista;

/**
 * Per i test dell'alchimista: la mandragola, quattro radici, invece di un ingrediente pescato a caso.
 */
final class Alchimie {

    static final String MANDRAGOLA = "F;radice di mandragola;radici di mandragola;RADURA BOSCO;E quando le tiriamo su non strillano?;Solo un po'.";
    static final int RADICI = 4;

    private Alchimie() {
    }

    /**
     * Fissa la mandragola come ingrediente dell'alchimista della partita, prima che l'incarico si offra.
     */
    static LAlchimista conLaMandragola() {
        LAlchimista alchimista = RegistroMissioni.getTutteLeMissioni().stream().filter(LAlchimista.class::isInstance)
                .map(LAlchimista.class::cast).findFirst().orElseThrow(AssertionError::new);
        alchimista.aggiungiProprieta("PARAMETRO_" + LAlchimista.INGREDIENTE, MANDRAGOLA);
        alchimista.aggiungiProprieta("PARAMETRO_QUANTITA", String.valueOf(RADICI));
        return alchimista;
    }
}
