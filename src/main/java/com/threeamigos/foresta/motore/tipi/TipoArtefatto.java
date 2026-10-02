package com.threeamigos.foresta.motore.tipi;

/**
 *
 * @author Stefano Reksten
 */
public enum TipoArtefatto {

    SPADA(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.TAGLIENTE,"Spada", "impugna"),
    MAZZA(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.CONTUNDENTE, "Mazza", "brandisce"),
    ASCIA(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.TAGLIENTE, "Ascia", "impugna"),
    // Arma a due mani.
    SPADONE(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ENTRAMBE_LE_MANI, TipoDanno.TAGLIENTE, "Spadone", "impugna"),
    LANCIA(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.PERFORANTE, "Lancia", "impugna"),

    BASTONE_MAGICO(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.CONTUNDENTE, "Bastone magico", "impugna"),

    LIBRO_MAGICO(SupertipoArtefatto.POTENZIAMENTO_POTERE_MAGICO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_SECONDARIA, "Libro magico", "porta"),

    SCUDO(SupertipoArtefatto.SCUDO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_SECONDARIA, "Scudo", "porta"),

    ELMO(SupertipoArtefatto.ELMO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.TESTA, "Elmo", "indossa"),

    ARMATURA(SupertipoArtefatto.ARMATURA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.CORPO, "Armatura", "indossa"),
    VESTE(SupertipoArtefatto.ARMATURA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.CORPO, "Veste", "indossa"),

    SCHINIERI(SupertipoArtefatto.SCHINIERI, TipoCardinalitaArtefatto.PAIO, TipoSlotArtefatto.GAMBE, "Schinieri", "indossa"),

    ANELLO(SupertipoArtefatto.ALTRO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ACCESSORIO, "Anello", "indossa"),
    TALISMANO(SupertipoArtefatto.ALTRO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ACCESSORIO, "Talismano", "possiede"),
    NINNOLO(SupertipoArtefatto.ALTRO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ACCESSORIO, "Ninnolo", "ha con se"),

    // Ingredienti magici: si comprano dal venditore di pergamene e si fondono sugli artefatti dall'incantatore.
    // Ognuno ha la sua specialità (vedi GeneratoreArtefattiTabelle e ingredienti.txt).
    PERGAMENA(SupertipoArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO, "Pergamena", "porta con se"),
    GEMMA(SupertipoArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO, "Gemma", "porta con se"),
    MONILE(SupertipoArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO, "Monile", "porta con se"),
    GINGILLO(SupertipoArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO, "Gingillo", "porta con se"),
    SIGILLO(SupertipoArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO, "Sigillo", "porta con se");

    private final SupertipoArtefatto supertipo;
    private final TipoCardinalitaArtefatto cardinalita;
    private final TipoSlotArtefatto tipoSlotArtefatto;
    private final TipoDanno tipoDanno;
    private final String descrizione;
    private final String utilizzo;

    TipoArtefatto(SupertipoArtefatto supertipo, TipoCardinalitaArtefatto cardinalita, TipoSlotArtefatto tipoSlotArtefatto, TipoDanno tipoDanno,
                  String descrizione, String utilizzo) {
        if (supertipo != SupertipoArtefatto.ARMA) {
            throw new IllegalArgumentException("Il Supertipo per l'artefatto deve essere ARMA");
        }
        this.supertipo = supertipo;
        this.cardinalita = cardinalita;
        this.tipoSlotArtefatto = tipoSlotArtefatto;
        this.tipoDanno = tipoDanno;
        this.descrizione = descrizione;
        this.utilizzo = utilizzo;
    }

    TipoArtefatto(SupertipoArtefatto supertipo, TipoCardinalitaArtefatto cardinalita, TipoSlotArtefatto tipoSlotArtefatto,
                  String descrizione, String utilizzo) {
        if (supertipo == SupertipoArtefatto.ARMA) {
            throw new IllegalArgumentException("Se il Supertipo per l'artefatto è ARMA occorre passare anche il TipoDanno");
        }
        this.supertipo = supertipo;
        this.cardinalita = cardinalita;
        this.tipoSlotArtefatto = tipoSlotArtefatto;
        this.tipoDanno = null;
        this.descrizione = descrizione;
        this.utilizzo = utilizzo;
    }

    /**
     * Pergamena, gemma, monile, gingillo o sigillo: un ingrediente magico da fondere su un artefatto
     */
    public boolean isIngrediente() {
        return supertipo == SupertipoArtefatto.INCANTAMENTO;
    }

    public SupertipoArtefatto getSupertipo() {
        return supertipo;
    }

    public TipoCardinalitaArtefatto getCardinalita() {
        return cardinalita;
    }

    public TipoSlotArtefatto getSlotArtefatto() {
        return tipoSlotArtefatto;
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
