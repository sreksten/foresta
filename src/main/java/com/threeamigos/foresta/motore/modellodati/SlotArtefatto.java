package com.threeamigos.foresta.motore.modellodati;

/**
 *
 * @author Stefano Reksten
 */
public enum SlotArtefatto {

    /**
     * Massimo 1 Elmo
     */
    TESTA,
    /**
     * Massimo 1 Armatura o Veste
     */
    CORPO,
    /**
     * Massimo 1 Arma (Spada, Mazza, Bastone magico...)
     */
    MANO_PRINCIPALE,
    /**
     * Massimo 1 Scudo / Libro magico / Seconda spada per Ladro o Elfo
     */
    MANO_SECONDARIA,
    /**
     * Per ora niente massimo (Anelli, Talismani, Ninnoli - non occupano le mani)
     */
    ACCESSORIO,
    /**
     * Tipo particolare, destinato a potenziare le armi (Incantamenti/Rune) e rimane nell'inventario di gruppo
     */
    NUCLEO

}
