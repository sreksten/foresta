package com.threeamigos.foresta.artefatti.tipi;

import com.threeamigos.foresta.artefatti.Arma;

/**
 * I posti in cui un personaggio porta un artefatto, ciascuno con il proprio limite: un elmo in testa, un'arma per mano,
 * un paio di schinieri sulle gambe e così via.
 * <p>
 * Un'arma a due mani occupa {@link #ENTRAMBE_LE_MANI}, cioè sia la mano principale sia la secondaria. {@link #NUCLEO}
 * non è un posto del corpo: segna gli ingredienti magici, che restano sempre nell'inventario del gruppo.
 *
 * @author Stefano Reksten
 */
public enum TipoSlotArtefatto {

    /**
     * Massimo 1 Elmo
     */
    TESTA,
    /**
     * Massimo 1 Maschera
     */
    VOLTO,
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
     * Armi a due mani (Spadone, Falce): occupa sia la mano principale sia la secondaria
     */
    ENTRAMBE_LE_MANI,
    /**
     * Massimo un paio di schinieri
     */
    GAMBE,
    /**
     * Per ora niente massimo (Anelli, Talismani, Ninnoli - non occupano le mani)
     */
    ACCESSORIO,
    /**
     * Tipo particolare, destinato a potenziare le armi (Incantamenti/Rune) e rimane nell'inventario di gruppo
     */
    NUCLEO

}
