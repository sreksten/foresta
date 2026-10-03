package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.missioni.Mandante;
import com.threeamigos.foresta.missioni.RichiestaDiMateriali;

/**
 * Per i test delle richieste di materiali: l'alchimista della partita, e la mandragola, quattro radici, invece di un
 * materiale pescato a caso.
 */
final class Alchimie {

    static final String MANDRAGOLA = "F;radice di mandragola;radici di mandragola;LUOGHI RADURA BOSCO;3-5;5;E quando le tiriamo su non strillano?;Solo un po'.";
    static final int RADICI = 4;

    private Alchimie() {
    }

    /**
     * La richiesta di materiali di un mandante, nella partita.
     */
    static RichiestaDiMateriali richiestaDi(Mandante mandante) {
        return RegistroMissioni.getTutteLeMissioni().stream().filter(RichiestaDiMateriali.class::isInstance)
                .map(RichiestaDiMateriali.class::cast).filter(r -> r.getMandante() == mandante)
                .findFirst().orElseThrow(AssertionError::new);
    }

    static RichiestaDiMateriali alchimista() {
        return richiestaDi(Mandante.ALCHIMISTA);
    }

    /**
     * Fissa il materiale e la quantità di una richiesta, prima che l'incarico si offra.
     */
    static RichiestaDiMateriali fissa(RichiestaDiMateriali richiesta, String materiale, int quantita) {
        richiesta.aggiungiProprieta("PARAMETRO_" + RichiestaDiMateriali.MATERIALE, materiale);
        richiesta.aggiungiProprieta("PARAMETRO_QUANTITA", String.valueOf(quantita));
        return richiesta;
    }

    /**
     * L'alchimista della partita, con la mandragola.
     */
    static RichiestaDiMateriali conLaMandragola() {
        return fissa(alchimista(), MANDRAGOLA, RADICI);
    }
}
