package com.threeamigos.foresta.testi;

/**
 * Come {@link GrammarBean} sceglie un'alternativa fra i figli di una produzione.
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
