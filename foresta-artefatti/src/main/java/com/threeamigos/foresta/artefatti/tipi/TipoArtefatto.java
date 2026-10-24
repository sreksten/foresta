package com.threeamigos.foresta.artefatti.tipi;

import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.testi.Testi;

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

    /**
     * Una spada, a una mano: danno tagliente.
     */
    SPADA(CategoriaArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.TAGLIENTE),
    /**
     * Una mazza, a una mano: danno contundente.
     */
    MAZZA(CategoriaArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.CONTUNDENTE),
    /**
     * Un'ascia, a una mano: danno tagliente.
     */
    ASCIA(CategoriaArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.TAGLIENTE),
    /** Arma a due mani. */
    SPADONE(CategoriaArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ENTRAMBE_LE_MANI, TipoDanno.TAGLIENTE),
    /** Arma a due mani, come lo spadone: la falce e il falcione. */
    FALCE(CategoriaArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ENTRAMBE_LE_MANI, TipoDanno.TAGLIENTE),
    /**
     * Una lancia, a una mano: danno perforante.
     */
    LANCIA(CategoriaArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.PERFORANTE),

    /**
     * Un bastone magico: danno contundente, aumenta il potere magico.
     */
    BASTONE_MAGICO(CategoriaArtefatto.ARMA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_PRINCIPALE, TipoDanno.CONTUNDENTE),

    /**
     * Un libro magico: aumenta il potere magico, si tiene nella mano secondaria.
     */
    LIBRO_MAGICO(CategoriaArtefatto.POTENZIAMENTO_POTERE_MAGICO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_SECONDARIA),

    /**
     * Uno scudo, nella mano secondaria.
     */
    SCUDO(CategoriaArtefatto.SCUDO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.MANO_SECONDARIA),

    /**
     * Un elmo, per la testa.
     */
    ELMO(CategoriaArtefatto.ELMO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.TESTA),

    /**
     * Un'armatura, per il corpo.
     */
    ARMATURA(CategoriaArtefatto.ARMATURA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.CORPO),
    /**
     * Una veste, per il corpo: protegge dalla magia.
     */
    VESTE(CategoriaArtefatto.ARMATURA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.CORPO),
    /**
     * Una maschera, per il volto.
     */
    MASCHERA(CategoriaArtefatto.ARMATURA, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.VOLTO),

    /**
     * Un paio di schinieri, per le gambe.
     */
    SCHINIERI(CategoriaArtefatto.SCHINIERI, TipoCardinalitaArtefatto.PAIO, TipoSlotArtefatto.GAMBE),

    /**
     * Un anello, un accessorio.
     */
    ANELLO(CategoriaArtefatto.ALTRO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ACCESSORIO),
    /**
     * Un talismano, un accessorio.
     */
    TALISMANO(CategoriaArtefatto.ALTRO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ACCESSORIO),
    /**
     * Un ninnolo, un accessorio.
     */
    NINNOLO(CategoriaArtefatto.ALTRO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.ACCESSORIO),

    // Ingredienti magici: si comprano dal venditore di pergamene e si fondono sugli artefatti dall'incantatore.
    // Ognuno ha la sua specialità (vedi GeneratoreArtefattiTabelle e ingredienti.txt).
    /**
     * Una pergamena: un ingrediente magico che dà modificatori alle caratteristiche primarie.
     */
    PERGAMENA(CategoriaArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),
    /**
     * Una gemma: un ingrediente magico che dà incantamenti elementali.
     */
    GEMMA(CategoriaArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),
    /**
     * Un monile: un ingrediente magico che dà incantamenti magici.
     */
    MONILE(CategoriaArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),
    /**
     * Un gingillo: un ingrediente magico che dà modificatori agli attributi d'attacco.
     */
    GINGILLO(CategoriaArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),
    /**
     * Un sigillo: un ingrediente magico che dà modificatori agli attributi di difesa.
     */
    SIGILLO(CategoriaArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),

    // Un oggetto che sul banco dell'incantatore fa salire di un livello un artefatto, invece di dargli degli effetti: si trova
    // solo nei cofani, non si compra né si vende (vedi isAggiornamento, RegoleIncantatura e artefatti2.txt per i nomi).
    /**
     * Un oggetto che fa salire di un livello un artefatto.
     */
    AGGIORNAMENTO(CategoriaArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO),

    // Un libro leggendario che sul banco dell'incantatore, invece di dare effetti, riprogramma l'artefatto: lo spezza negli
    // ingredienti dei suoi incantamenti. Si trova solo nei cofani, non si compra né si vende (vedi isRiprogrammazione,
    // RegoleIncantatura e LibroTecnico: è The C Programming Language).
    /**
     * Un libro che riprogramma un artefatto, spezzandolo negli ingredienti dei suoi incantamenti.
     */
    RIPROGRAMMAZIONE(CategoriaArtefatto.INCANTAMENTO, TipoCardinalitaArtefatto.SINGOLO, TipoSlotArtefatto.NUCLEO);

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

    /**
     * La categoria dell'artefatto.
     */
    public CategoriaArtefatto getCategoria() {
        return categoriaArtefatto;
    }

    /**
     * Se si conta per singolo pezzo o per paio.
     */
    public TipoCardinalitaArtefatto getCardinalita() {
        return cardinalita;
    }

    /**
     * Lo slot in cui si equipaggia.
     */
    public TipoSlotArtefatto getSlotArtefatto() {
        return tipoSlotArtefatto;
    }

    /**
     * Il tipo di danno fisico, per le armi; null per gli altri.
     */
    public TipoDanno getTipoDanno() {
        return tipoDanno;
    }

    /**
     * La descrizione del tipo, per i testi.
     */
    public String getDescrizione() {
        return Testi.testoOCostante(this, "DESCRIZIONE");
    }

    /**
     * A che cosa serve, per i testi.
     */
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

    /**
     * Un libro che riprogramma l'artefatto su cui lo si fonde: lo scinde nell'artefatto senza incantamenti e in un ingrediente per
     * ogni incantamento che aveva. È anche un ingrediente (si mette sul banco dell'incantatore come gli altri), ma non lo si
     * compra né lo si vende, e si ritrova intero dopo la fusione.
     */
    public boolean isRiprogrammazione() {
        return this == RIPROGRAMMAZIONE;
    }

}
