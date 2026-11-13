package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.GestoreGrammatiche;

/**
 * Come {@link GestoreGrammatiche} sceglie un'alternativa fra i figli di una produzione.
 *
 * @author Stefano Reksten
 */
public enum TipoModalitaProduzioneTesti {

    /**
     * Sceglie sempre la prima alternativa.
     */
    PRIMO,
    /**
     * Sceglie sempre l'ultima alternativa.
     */
    ULTIMO,
    /**
     * Sceglie una alternativa a caso.
     */
    CASUALE

}
