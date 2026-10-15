package com.threeamigos.foresta.tipi;

/**
 * I tipi di artefatto, dalla spada alla pergamena. Ogni tipo dichiara il suo {@link SupertipoArtefatto supertipo}, la
 * {@link TipoCardinalitaArtefatto cardinalità} (singolo o a paio), lo {@link TipoSlotArtefatto slot} che occupa, per le
 * armi il {@link TipoDanno tipo di danno}, il nome generico e il verbo con cui un personaggio lo usa (impugna, indossa...).
 * <p>
 * Gli <b>ingredienti magici</b> (pergamena, gemma, monile, gingillo, sigillo) non si indossano: si fondono sugli
 * artefatti dall'incantatore.
 *
 * @author Stefano Reksten
 */
public enum TipoArtefatto {

    SPADA(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.TAGLIENTE,"Spada", "impugna"),
    MAZZA(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.CONTUNDENTE, "Mazza", "brandisce"),
    ASCIA(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.TAGLIENTE, "Ascia", "impugna"),
    /** Arma a due mani. */
    SPADONE(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ENTRAMBE_LE_MANI, TipoDanno.TAGLIENTE, "Spadone", "impugna"),
    LANCIA(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.PERFORANTE, "Lancia", "impugna"),

    BASTONE_MAGICO(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.CONTUNDENTE, "Bastone magico", "impugna"),

    LIBRO_MAGICO(SupertipoArtefatto.POTENZIAMENTO_POTERE_MAGICO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_SECONDARIA, "Libro magico", "porta"),

    SCUDO(SupertipoArtefatto.SCUDO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_SECONDARIA, "Scudo", "porta"),

    ELMO(SupertipoArtefatto.ELMO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.TESTA, "Elmo", "indossa"),

    ARMATURA(SupertipoArtefatto.ARMATURA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.CORPO, "Armatura", "indossa"),
    VESTE(SupertipoArtefatto.ARMATURA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.CORPO, "Veste", "indossa"),
    MASCHERA(SupertipoArtefatto.ARMATURA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.VOLTO, "Maschera", "indossa"),

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
    SIGILLO(SupertipoArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO, "Sigillo", "porta con se"),

    // Un oggetto che sul banco dell'incantatore fa salire di un livello un artefatto, invece di dargli degli effetti: si trova
    // solo nei cofani, non si compra né si vende (vedi isAggiornamento, RegoleIncantatura e artefatti2.txt per i nomi).
    AGGIORNAMENTO(SupertipoArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO, "Aggiornamento", "porta con se");

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

    /**
     * Un oggetto che fa salire di un livello l'artefatto su cui lo si fonde, invece di dargli degli effetti. È anche un
     * ingrediente (si mette sul banco dell'incantatore come gli altri), ma non lo si compra né lo si vende.
     */
    public boolean isAggiornamento() {
        return this == AGGIORNAMENTO;
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
