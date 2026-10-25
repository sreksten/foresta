package com.threeamigos.foresta.tipi;


/**
 * Quando un Artefatto ha un Modificatore di {@link TipoAttributo}, questo modificatore si applica a un bersaglio.
 * Il bersaglio può essere il portatore dell'artefatto stesso o, in caso di un'arma, chi viene colpito dall'arma stessa,
 * che <i>può</i> applicare il modificatore al bersaglio se questo fallisce il tiro salvezza.
 * <p>
 * Per il tipo di variazione, vedi {@link TipoModificatore}.
 * <p>
 * La durata di un modificatore applicata a un portatore è fissa (si applica fintantoché il Personaggio utilizza
 * quell'Artefatto), quella applicata a un bersaglio solitamente è variabile.
 *
 * @author Stefano Reksten
 */
public enum BersaglioModificatore {

    /**
     * Chi porta l'artefatto
     */
    PORTATORE,

    /**
     * Chi viene colpito dall'arma (vale solo per le armi)
     */
    BERSAGLIO

}
