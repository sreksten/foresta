package com.threeamigos.foresta.tipi;

/**
 * In che cosa si misura il tempo che resta a un modificatore di attributo.
 *
 * @author Stefano Reksten
 */
public enum UnitaDurataModificatore {

    /**
     * Non scade mai
     */
    PERMANENTE,
    /**
     * Scade con i turni di combattimento (come gli effetti di stato)
     */
    TURNI,
    /**
     * Scade con le locazioni (come i personaggi a tempo)
     */
    LOCAZIONI

}
