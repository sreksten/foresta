package com.threeamigos.foresta.tipi;

/**
 * I tipi di artefatto, dalla spada alla pergamena. Ogni tipo dichiara il suo {@link SupertipoArtefatto supertipo}, la
 * {@link TipoCardinalitaArtefatto cardinalità} (singolo o a paio), lo {@link TipoSlotArtefatto slot} che occupa, per le
 * armi il {@link TipoDanno tipo di danno}, il nome generico e il verbo con cui un personaggio lo usa (impugna, indossa...).
 * <p>
 * Gli <b>ingredienti magici</b> (pergamena, gemma, monile, gingillo, sigillo) non si indossano: si fondono sugli
 * artefatti dall'incantatore.
 * <p>
 * I testi di ogni costante (DESCRIZIONE, UTILIZZO) non stanno qui ma nel file {@code tipo_artefatto_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 *
 * @author Stefano Reksten
 */
public enum TipoArtefatto {

    SPADA(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.TAGLIENTE),
    MAZZA(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.CONTUNDENTE),
    ASCIA(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.TAGLIENTE),
    /** Arma a due mani. */
    SPADONE(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ENTRAMBE_LE_MANI, TipoDanno.TAGLIENTE),
    LANCIA(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.PERFORANTE),

    BASTONE_MAGICO(SupertipoArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.CONTUNDENTE),

    LIBRO_MAGICO(SupertipoArtefatto.POTENZIAMENTO_POTERE_MAGICO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_SECONDARIA),

    SCUDO(SupertipoArtefatto.SCUDO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_SECONDARIA),

    ELMO(SupertipoArtefatto.ELMO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.TESTA),

    ARMATURA(SupertipoArtefatto.ARMATURA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.CORPO),
    VESTE(SupertipoArtefatto.ARMATURA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.CORPO),
    MASCHERA(SupertipoArtefatto.ARMATURA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.VOLTO),

    SCHINIERI(SupertipoArtefatto.SCHINIERI, TipoCardinalitaArtefatto.PAIO, TipoSlotArtefatto.GAMBE),

    ANELLO(SupertipoArtefatto.ALTRO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ACCESSORIO),
    TALISMANO(SupertipoArtefatto.ALTRO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ACCESSORIO),
    NINNOLO(SupertipoArtefatto.ALTRO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ACCESSORIO),

    // Ingredienti magici: si comprano dal venditore di pergamene e si fondono sugli artefatti dall'incantatore.
    // Ognuno ha la sua specialità (vedi GeneratoreArtefattiTabelle e ingredienti.txt).
    PERGAMENA(SupertipoArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),
    GEMMA(SupertipoArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),
    MONILE(SupertipoArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),
    GINGILLO(SupertipoArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),
    SIGILLO(SupertipoArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),

    // Un oggetto che sul banco dell'incantatore fa salire di un livello un artefatto, invece di dargli degli effetti: si trova
    // solo nei cofani, non si compra né si vende (vedi isAggiornamento, RegoleIncantatura e artefatti2.txt per i nomi).
    AGGIORNAMENTO(SupertipoArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO);

    private final SupertipoArtefatto supertipo;
    private final TipoCardinalitaArtefatto cardinalita;
    private final TipoSlotArtefatto tipoSlotArtefatto;
    private final TipoDanno tipoDanno;

    TipoArtefatto(SupertipoArtefatto supertipo, TipoCardinalitaArtefatto cardinalita, TipoSlotArtefatto tipoSlotArtefatto, TipoDanno tipoDanno) {
        if (supertipo != SupertipoArtefatto.ARMA) {
            throw new IllegalArgumentException("Il Supertipo per l'artefatto deve essere ARMA");
        }
        this.supertipo = supertipo;
        this.cardinalita = cardinalita;
        this.tipoSlotArtefatto = tipoSlotArtefatto;
        this.tipoDanno = tipoDanno;
    }

    TipoArtefatto(SupertipoArtefatto supertipo, TipoCardinalitaArtefatto cardinalita, TipoSlotArtefatto tipoSlotArtefatto) {
        if (supertipo == SupertipoArtefatto.ARMA) {
            throw new IllegalArgumentException("Se il Supertipo per l'artefatto è ARMA occorre passare anche il TipoDanno");
        }
        this.supertipo = supertipo;
        this.cardinalita = cardinalita;
        this.tipoSlotArtefatto = tipoSlotArtefatto;
        this.tipoDanno = null;
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
        return Testi.testoOCostante(this, "DESCRIZIONE");
    }

    public String getUtilizzo() {
        return Testi.testoOCostante(this, "UTILIZZO");
    }

}
