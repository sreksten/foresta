package com.threeamigos.foresta.motore.modellodati;

/**
 *
 * @author Stefano Reksten
 */
public enum TipoArtefatto {

    SPADA(SupertipoArtefatto.ARMA, TipoDanno.TAGLIENTE,"impugna"),
    MAZZA(SupertipoArtefatto.ARMA, TipoDanno.CONTUNDENTE, "brandisce"),
    ASCIA(SupertipoArtefatto.ARMA, TipoDanno.TAGLIENTE, "impugna"),
    LANCIA(SupertipoArtefatto.ARMA, TipoDanno.PERFORANTE, "impugna"),

    SCUDO(SupertipoArtefatto.ARMATURA, "porta"),
    ELMO(SupertipoArtefatto.ARMATURA, "indossa"),
    ARMATURA(SupertipoArtefatto.ARMATURA, "indossa"),

    ANELLO(SupertipoArtefatto.ALTRO, "indossa"),
    TALISMANO(SupertipoArtefatto.ALTRO, "possiede"),

    LIBRO_MAGICO(SupertipoArtefatto.ALTRO, "porta"),

    BASTONE_MAGICO(SupertipoArtefatto.ALTRO, "impugna"),

    VESTE(SupertipoArtefatto.ARMATURA, "indossa");

    private final SupertipoArtefatto supertipo;
    private final TipoDanno tipoDanno;
    private final String utilizzo;

    TipoArtefatto(SupertipoArtefatto supertipo, TipoDanno tipoDanno, String utilizzo) {
        if (supertipo != SupertipoArtefatto.ARMA) {
            throw new IllegalArgumentException("Il Supertipo per l'artefatto deve essere ARMA");
        }
        this.supertipo = supertipo;
        this.tipoDanno = tipoDanno;
        this.utilizzo = utilizzo;
    }

    TipoArtefatto(SupertipoArtefatto supertipo, String utilizzo) {
        if (supertipo == SupertipoArtefatto.ARMA) {
            throw new IllegalArgumentException("Se il Supertipo per l'artefatto è ARMA occorre passare anche il TipoDanno");
        }
        this.supertipo = supertipo;
        this.tipoDanno = null;
        this.utilizzo = utilizzo;
    }

    public SupertipoArtefatto getSupertipo() {
        return supertipo;
    }

    public String getUtilizzo() {
        return utilizzo;
    }

}
