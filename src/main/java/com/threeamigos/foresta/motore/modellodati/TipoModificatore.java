package com.threeamigos.foresta.motore.modellodati;

/**
 * Il tipo di modifica da portare a un certo attributo
 * @author Stefano Reksten
 */
public enum TipoModificatore {

    /**
     * Modifica un attributo di un numero fisso di punti
     */
    AUMENTO_FISSO,

    /**
     * Modifica un attributo di una certa percentuale
     */
    AUMENTO_PERCENTUALE,

    /**
     * Modifica un attributo portandolo a una data quantità fissa
     */
    QUANTITA_ASSOLUTA

}
