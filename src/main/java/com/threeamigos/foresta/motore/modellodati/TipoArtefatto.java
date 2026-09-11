package com.threeamigos.foresta.motore.modellodati;

/**
 *
 * @author Stefano Reksten
 */
public enum TipoArtefatto {

    SPADA(SupertipoArtefatto.ARMA, TipoDanno.TAGLIENTE,"Spada", "impugna"),
    MAZZA(SupertipoArtefatto.ARMA, TipoDanno.CONTUNDENTE, "Mazza", "brandisce"),
    ASCIA(SupertipoArtefatto.ARMA, TipoDanno.TAGLIENTE, "Ascia", "impugna"),
    LANCIA(SupertipoArtefatto.ARMA, TipoDanno.PERFORANTE, "Lancia", "impugna"),

    // FIXME se si passa ARMA occorre passare il tipo di danno, ma questo dipenderebbe un po' dall'incantesimo...
    LIBRO_MAGICO(SupertipoArtefatto.ALTRO, "Libro magico", "porta"),
    BASTONE_MAGICO(SupertipoArtefatto.ALTRO, "Bastone magico", "impugna"),

    SCUDO(SupertipoArtefatto.SCUDO, "Scudo", "porta"),

    ELMO(SupertipoArtefatto.ARMATURA, "Elmo", "indossa"),
    ARMATURA(SupertipoArtefatto.ARMATURA, "Armatura", "indossa"),
    VESTE(SupertipoArtefatto.ARMATURA, "Veste", "indossa"),

    ANELLO(SupertipoArtefatto.ALTRO, "Anello", "indossa"),
    TALISMANO(SupertipoArtefatto.ALTRO, "Talismano", "possiede"),
    NINNOLO(SupertipoArtefatto.ALTRO, "Ninnolo", "ha con se");

    private final SupertipoArtefatto supertipo;
    private final TipoDanno tipoDanno;
    private final String descrizione;
    private final String utilizzo;

    TipoArtefatto(SupertipoArtefatto supertipo, TipoDanno tipoDanno, String descrizione, String utilizzo) {
        if (supertipo != SupertipoArtefatto.ARMA) {
            throw new IllegalArgumentException("Il Supertipo per l'artefatto deve essere ARMA");
        }
        this.supertipo = supertipo;
        this.tipoDanno = tipoDanno;
        this.descrizione = descrizione;
        this.utilizzo = utilizzo;
    }

    TipoArtefatto(SupertipoArtefatto supertipo, String descrizione, String utilizzo) {
        if (supertipo == SupertipoArtefatto.ARMA) {
            throw new IllegalArgumentException("Se il Supertipo per l'artefatto è ARMA occorre passare anche il TipoDanno");
        }
        this.supertipo = supertipo;
        this.tipoDanno = null;
        this.descrizione = descrizione;
        this.utilizzo = utilizzo;
    }

    public SupertipoArtefatto getSupertipo() {
        return supertipo;
    }

    public TipoDanno getTipoDanno() {
        return tipoDanno;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getUtilizzo() {
        return utilizzo;
    }

}
