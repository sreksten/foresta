package com.threeamigos.foresta.motore.modellodati;

/**
 *
 * @author Stefano Reksten
 */
public enum TipoArtefatto {

    SPADA("impugna"),
    MAZZA("brandisce"),
    ASCIA("impugna"),
    LANCIA("impugna"),

    SCUDO("porta"),
    ELMO("indossa"),
    ARMATURA("indossa"),

    TALISMANO("possiede"),

    LIBRO_MAGICO("porta"),

    BASTONE_MAGICO("impugna"),

    VESTE("indossa");

    private final String utilizzo;

    TipoArtefatto(String utilizzo) {
        this.utilizzo = utilizzo;
    }

    public String getUtilizzo() {
        return utilizzo;
    }

}
