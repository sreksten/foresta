package com.threeamigos.foresta.motore.tipi;

import com.threeamigos.foresta.motore.GrammarBean;

/**
 * Controls how {@link GrammarBean} picks an alternative among a production's children.
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
