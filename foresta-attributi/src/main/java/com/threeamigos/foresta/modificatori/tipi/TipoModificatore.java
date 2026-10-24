package com.threeamigos.foresta.modificatori.tipi;

/**
 * Il tipo di modifica da portare a un certo attributo. Può essere una variazione di un numero fisso di punti,
 * una variazione di una certa percentuale o una modifica che porta l'attributo a una data quantità prefissata.
 *
 * @author Stefano Reksten
 */
public enum TipoModificatore {

    /**
     * Modifica un attributo di un numero fisso di punti
     */
    VARIAZIONE_FISSA,

    /**
     * Modifica un attributo di una certa percentuale
     */
    VARIAZIONE_PERCENTUALE,

    /**
     * Modifica un attributo portandolo a una data quantità fissa
     */
    QUANTITA_ASSOLUTA

}
