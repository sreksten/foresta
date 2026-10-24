package com.threeamigos.foresta.tipi;

/**
 * I tipi di artefatto, dalla spada alla pergamena. Ogni tipo dichiara la sua {@link CategoriaArtefatto}, la
 * {@link TipoCardinalitaArtefatto cardinalità} (singolo o a paio), lo {@link TipoSlotArtefatto slot} che occupa, per le
 * armi il {@link TipoDanno tipo di danno}, il nome generico e il verbo con cui un personaggio lo usa (impugna, indossa...).
 * <p>
 * Gli <b>ingredienti magici</b> (pergamena, gemma, monile, gingillo, sigillo) non si indossano, ma si fondono sugli
 * artefatti tramite l'incantatore.
 * <p>
 * I testi di ogni costante (DESCRIZIONE, UTILIZZO) non stanno qui ma nel file {@code tipo_artefatto_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 *
 * @author Stefano Reksten
 */
public enum TipoArtefatto {

    SPADA(CategoriaArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.TAGLIENTE),
    MAZZA(CategoriaArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.CONTUNDENTE),
    ASCIA(CategoriaArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.TAGLIENTE),
    /** Arma a due mani. */
    SPADONE(CategoriaArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ENTRAMBE_LE_MANI, TipoDanno.TAGLIENTE),
    /** Arma a due mani, come lo spadone: la falce e il falcione. */
    FALCE(CategoriaArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ENTRAMBE_LE_MANI, TipoDanno.TAGLIENTE),
    LANCIA(CategoriaArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.PERFORANTE),

    BASTONE_MAGICO(CategoriaArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.CONTUNDENTE),

    LIBRO_MAGICO(CategoriaArtefatto.POTENZIAMENTO_POTERE_MAGICO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_SECONDARIA),

    SCUDO(CategoriaArtefatto.SCUDO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_SECONDARIA),

    ELMO(CategoriaArtefatto.ELMO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.TESTA),

    ARMATURA(CategoriaArtefatto.ARMATURA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.CORPO),
    VESTE(CategoriaArtefatto.ARMATURA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.CORPO),
    MASCHERA(CategoriaArtefatto.ARMATURA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.VOLTO),

    SCHINIERI(CategoriaArtefatto.SCHINIERI, TipoCardinalitaArtefatto.PAIO, TipoSlotArtefatto.GAMBE),

    ANELLO(CategoriaArtefatto.ALTRO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ACCESSORIO),
    TALISMANO(CategoriaArtefatto.ALTRO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ACCESSORIO),
    NINNOLO(CategoriaArtefatto.ALTRO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ACCESSORIO),

    // Ingredienti magici: si comprano dal venditore di pergamene e si fondono sugli artefatti dall'incantatore.
    // Ognuno ha la sua specialità (vedi GeneratoreArtefattiTabelle e ingredienti.txt).
    PERGAMENA(CategoriaArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),
    GEMMA(CategoriaArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),
    MONILE(CategoriaArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),
    GINGILLO(CategoriaArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),
    SIGILLO(CategoriaArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),

    // Un oggetto che sul banco dell'incantatore fa salire di un livello un artefatto, invece di dargli degli effetti: si trova
    // solo nei cofani, non si compra né si vende (vedi isAggiornamento, RegoleIncantatura e artefatti2.txt per i nomi).
    AGGIORNAMENTO(CategoriaArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO);

    private final CategoriaArtefatto categoriaArtefatto;
    private final TipoCardinalitaArtefatto cardinalita;
    private final TipoSlotArtefatto tipoSlotArtefatto;
    private final TipoDanno tipoDanno;

    // Un'arma ha sempre un tipo di danno, il resto mai: lo controlla TipoArtefattoTest su tutte le costanti
    TipoArtefatto(CategoriaArtefatto categoriaArtefatto, TipoCardinalitaArtefatto cardinalita, TipoSlotArtefatto tipoSlotArtefatto, TipoDanno tipoDanno) {
        this.categoriaArtefatto = categoriaArtefatto;
        this.cardinalita = cardinalita;
        this.tipoSlotArtefatto = tipoSlotArtefatto;
        this.tipoDanno = tipoDanno;
    }

    TipoArtefatto(CategoriaArtefatto categoriaArtefatto, TipoCardinalitaArtefatto cardinalita, TipoSlotArtefatto tipoSlotArtefatto) {
        this(categoriaArtefatto, cardinalita, tipoSlotArtefatto, null);
    }

    public CategoriaArtefatto getCategoria() {
        return categoriaArtefatto;
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

    /**
     * Pergamena, gemma, monile, gingillo o sigillo: un ingrediente magico da fondere su un artefatto
     */
    public boolean isIngrediente() {
        return categoriaArtefatto == CategoriaArtefatto.INCANTAMENTO;
    }

    /**
     * Se il generatore può produrne a caso (nel loot, nei negozi, nei cofani): no per la falce, che esiste solo come leggendario di bottino
     * (la Falce del Tristo Mietitore, vedi leggendari.txt).
     */
    public boolean isGenerabileACaso() {
        return this != FALCE;
    }

    /**
     * Un oggetto che fa salire di un livello l'artefatto su cui lo si fonde, invece di dargli degli effetti. È anche un
     * ingrediente (si mette sul banco dell'incantatore come gli altri), ma non lo si compra né lo si vende.
     */
    public boolean isAggiornamento() {
        return this == AGGIORNAMENTO;
    }

}
