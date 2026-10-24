package com.threeamigos.foresta.danni.tipi;

import com.threeamigos.foresta.effettidistato.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.testi.Testi;
import java.util.*;

/**
 * Le reazioni fra un tipo di danno e uno stato del difensore: una reazione scatta se il difensore ha almeno uno degli stati
 * delle precondizioni e riceve uno dei tipi di danno degli innescanti (vedi CalcolatoreCombattimento.interazioniAttive, che
 * le prova in sequenza). <b>L'ordine di dichiarazione è l'ordine in cui le reazioni si applicano</b>, per stato. (Le debolezze
 * e le resistenze della classe a un tipo di danno non sono qui: vedi TipoPersonaggio.getMoltiplicatoreDannoSubito.)
 * <p>
 * Quando scatta, una reazione può togliere al difensore degli stati e metterne altri ({@link #getStatiRimossi()},
 * {@link #getStatiApplicati()}): l'INCIAMPO atterra chi era RALLENTATO, il CONGELAMENTO trasforma il BAGNATO in CONGELATO.
 * Sono scritti qui, e non solo nel calcolo del danno, perché li usano sia il calcolo vero sia le tattiche, che guardano
 * oltre il colpo di adesso (CalcolatoreCombattimento.statiCausabili). Durate e danni periodici degli stati messi sono
 * quelli normali, salvo le eccezioni che il calcolo del danno conosce (l'ALIMENTAZIONE_FIAMMA allunga il BRUCIATO che
 * c'è già, lo SCHIACCIAMENTO e il FANGO allungano i loro stati, il
 * SOVRACCARICO_MENTALE e il DISORIENTAMENTO stordiscono per poco, la TOSSICITA_SETTICA raddoppia il danno del veleno).
 * <p>
 * I testi di ogni costante (DESCRIZIONE) non stanno qui ma nel file {@code tipo_interazione_con_effetti_di_stato_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 *
 * @author Stefano Reksten
 */
public enum TipoInterazioneConEffettiDiStato {

    // Interazioni con BAGNATO
    // + FULMINE
    /** Se il difensore è {@code BAGNATO} e riceve un danno {@code FULMINE}: il danno si moltiplica per 1,5. */
    ELETTROCUZIONE(
            Collections.singletonList(TipoEffettoDiStato.BAGNATO),
            Collections.singletonList(TipoDanno.FULMINE),
            1.5d,
            Collections.emptyList(),
            Collections.emptyList()),
    // + GELO
    /** Se il difensore è {@code BAGNATO} e riceve un danno {@code GELO}: perde {@code BAGNATO}; riceve {@code CONGELATO}. */
    CONGELAMENTO(
            Collections.singletonList(TipoEffettoDiStato.BAGNATO),
            Collections.singletonList(TipoDanno.GELO),
            1.0d,
            Collections.singletonList(TipoEffettoDiStato.BAGNATO),
            Collections.singletonList(TipoEffettoDiStato.CONGELATO)),
    // + FUOCO
    /** Se il difensore è {@code BAGNATO} e riceve un danno {@code FUOCO}: il danno si moltiplica per 0,5; perde {@code BAGNATO}; riceve {@code ACCECATO}. */
    VAPORIZZAZIONE(
            Collections.singletonList(TipoEffettoDiStato.BAGNATO),
            Collections.singletonList(TipoDanno.FUOCO),
            0.5d,
            Collections.singletonList(TipoEffettoDiStato.BAGNATO),
            Collections.singletonList(TipoEffettoDiStato.ACCECATO)),

    // Interazioni con BRUCIATO
    // + ARIA
    /** Se il difensore è {@code BRUCIATO} e riceve un danno {@code ARIA}: il danno si moltiplica per 1,5; riceve {@code BRUCIATO}. */
    ALIMENTAZIONE_FIAMMA(
            Collections.singletonList(TipoEffettoDiStato.BRUCIATO),
            Collections.singletonList(TipoDanno.ARIA),
            1.5d,
            Collections.emptyList(),
            Collections.singletonList(TipoEffettoDiStato.BRUCIATO)),
    // + ACQUA
    /** Se il difensore è {@code BRUCIATO} e riceve un danno {@code ACQUA}: perde {@code BRUCIATO} e {@code BAGNATO}. */
    ESTINZIONE(
            Collections.singletonList(TipoEffettoDiStato.BRUCIATO),
            Collections.singletonList(TipoDanno.ACQUA),
            1.0d,
            Arrays.asList(TipoEffettoDiStato.BRUCIATO, TipoEffettoDiStato.BAGNATO),
            Collections.emptyList()),
    // + GELO
    /** Se il difensore è {@code BRUCIATO} e riceve un danno {@code GELO}: il danno si moltiplica per 1,5; perde {@code BRUCIATO}; riceve {@code BAGNATO}. */
    SCIOGLIMENTO_TERMICO(
            Collections.singletonList(TipoEffettoDiStato.BRUCIATO),
            Collections.singletonList(TipoDanno.GELO),
            1.5d,
            Collections.singletonList(TipoEffettoDiStato.BRUCIATO),
            Collections.singletonList(TipoEffettoDiStato.BAGNATO)),
    // + VELENO
    /** Se il difensore è {@code BRUCIATO} e riceve un danno {@code VELENO}: il danno si moltiplica per 1,3; perde {@code BRUCIATO} e {@code AVVELENATO}. */
    ESPLOSIONE_DI_GAS(
            Collections.singletonList(TipoEffettoDiStato.BRUCIATO),
            Collections.singletonList(TipoDanno.VELENO),
            1.3d,
            Arrays.asList(TipoEffettoDiStato.BRUCIATO, TipoEffettoDiStato.AVVELENATO),
            Collections.emptyList()),

    // Interazioni con AVVELENATO
    // + FUOCO
    /** Se il difensore è {@code AVVELENATO} e riceve un danno {@code FUOCO}: il danno si moltiplica per 1,3; perde {@code AVVELENATO}; riceve {@code BRUCIATO}. */
    VAMPATA_TOSSICA(
            Collections.singletonList(TipoEffettoDiStato.AVVELENATO),
            Collections.singletonList(TipoDanno.FUOCO),
            1.3d,
            Collections.singletonList(TipoEffettoDiStato.AVVELENATO),
            Collections.singletonList(TipoEffettoDiStato.BRUCIATO)),
    // + ACIDO
    /** Se il difensore è {@code AVVELENATO} e riceve un danno {@code ACIDO}: il danno si moltiplica per 1,15. */
    REAZIONE_TOSSICA(
            Collections.singletonList(TipoEffettoDiStato.AVVELENATO),
            Collections.singletonList(TipoDanno.ACIDO),
            1.15d,
            Collections.emptyList(),
            Collections.emptyList()),

    // Interazioni con SANGUINAMENTO
    // + ACQUA
    /** Se il difensore è {@code SANGUINAMENTO} e riceve un danno {@code ACQUA}: il danno si moltiplica per 1,05. */
    DILUIZIONE_EMATICA(
            Collections.singletonList(TipoEffettoDiStato.SANGUINAMENTO),
            Collections.singletonList(TipoDanno.ACQUA),
            1.05d,
            Collections.emptyList(),
            Collections.emptyList()),
    // + GELO
    /** Se il difensore è {@code SANGUINAMENTO} e riceve un danno {@code GELO}: il danno si moltiplica per 1,05; perde {@code SANGUINAMENTO}; riceve {@code RALLENTATO}. */
    COAGULAZIONE_FORZATA(
            Collections.singletonList(TipoEffettoDiStato.SANGUINAMENTO),
            Collections.singletonList(TipoDanno.GELO),
            1.05d,
            Collections.singletonList(TipoEffettoDiStato.SANGUINAMENTO),
            Collections.singletonList(TipoEffettoDiStato.RALLENTATO)),

    // Interazioni con CONGELATO
    // + CONTUNDENTE
    /** Se il difensore è {@code CONGELATO} e riceve un danno {@code CONTUNDENTE}: perde {@code CONGELATO}. */
    FRANTUMAZIONE_DEL_GHIACCIO(
            Collections.singletonList(TipoEffettoDiStato.CONGELATO),
            Collections.singletonList(TipoDanno.CONTUNDENTE),
            1.0d,
            Collections.singletonList(TipoEffettoDiStato.CONGELATO),
            Collections.emptyList()),
    // + FUOCO
    /** Se il difensore è {@code CONGELATO} e riceve un danno {@code FUOCO}: il danno si moltiplica per 1,5; perde {@code CONGELATO}. */
    DISGELO_VIOLENTO(
            Collections.singletonList(TipoEffettoDiStato.CONGELATO),
            Collections.singletonList(TipoDanno.FUOCO),
            1.5d,
            Collections.singletonList(TipoEffettoDiStato.CONGELATO),
            Collections.emptyList()),
    // + FULMINE
    /** Se il difensore è {@code CONGELATO} e riceve un danno {@code FULMINE}: il danno si moltiplica per 1,3. */
    SUPERCONDUZIONE(
            Collections.singletonList(TipoEffettoDiStato.CONGELATO),
            Collections.singletonList(TipoDanno.FULMINE),
            1.3d,
            Collections.emptyList(),
            Collections.emptyList()),

    // Interazioni con MALEDETTO
    // + NECROTICO
    /** Se il difensore è {@code MALEDETTO} e riceve un danno {@code NECROTICO}: il danno si moltiplica per 2. */
    MIETITURA(
            Collections.singletonList(TipoEffettoDiStato.MALEDETTO),
            Collections.singletonList(TipoDanno.NECROTICO),
            2.0d,
            Collections.emptyList(),
            Collections.emptyList()),
    // + SACRO
    /** Se il difensore è {@code MALEDETTO} e riceve un danno {@code SACRO}: perde {@code MALEDETTO}. */
    RIGETTO(
            Collections.singletonList(TipoEffettoDiStato.MALEDETTO),
            Collections.singletonList(TipoDanno.SACRO),
            1.0d,
            Collections.singletonList(TipoEffettoDiStato.MALEDETTO),
            Collections.emptyList()),
    // + VUOTO
    /** Se il difensore è {@code MALEDETTO} e riceve un danno {@code VUOTO}: perde {@code MALEDETTO}. */
    COLLASSO_ENTROPICO(
            Collections.singletonList(TipoEffettoDiStato.MALEDETTO),
            Collections.singletonList(TipoDanno.VUOTO),
            1.0d,
            Collections.singletonList(TipoEffettoDiStato.MALEDETTO),
            Collections.emptyList()),

    // Interazioni con INFETTATO
    // + SACRO
    /** Se il difensore è {@code INFETTATO} e riceve un danno {@code SACRO}: il danno si moltiplica per 1,5; perde {@code INFETTATO}. */
    PURIFICAZIONE(
            Collections.singletonList(TipoEffettoDiStato.INFETTATO),
            Collections.singletonList(TipoDanno.SACRO),
            1.5d,
            Collections.singletonList(TipoEffettoDiStato.INFETTATO),
            Collections.emptyList()),
    // + ARCANO
    /** Se il difensore è {@code INFETTATO} e riceve un danno {@code ARCANO}: nessun cambiamento di stato né di danno. */
    SIFONE_VITALE(
            Collections.singletonList(TipoEffettoDiStato.INFETTATO),
            Collections.singletonList(TipoDanno.ARCANO),
            1.0d,
            Collections.emptyList(),
            Collections.emptyList()),
    // + VELENO
    /** Se il difensore è {@code INFETTATO} e riceve un danno {@code VELENO}: riceve {@code AVVELENATO}. */
    TOSSICITA_SETTICA(
            Collections.singletonList(TipoEffettoDiStato.INFETTATO),
            Collections.singletonList(TipoDanno.VELENO),
            1.0d,
            Collections.emptyList(),
            Collections.singletonList(TipoEffettoDiStato.AVVELENATO)),

    // Interazioni con STORDITO / ATTERRATO (fisica, non elementale)
    // + TAGLIENTE / PERFORANTE
    /** Se il difensore è {@code STORDITO} o {@code ATTERRATO} e riceve un danno {@code TAGLIENTE} o {@code PERFORANTE}: nessun cambiamento di stato né di danno. */
    COLPO_DI_GRAZIA(
            Arrays.asList(TipoEffettoDiStato.STORDITO, TipoEffettoDiStato.ATTERRATO),
            Arrays.asList(TipoDanno.TAGLIENTE, TipoDanno.PERFORANTE),
            1.0d,
            Collections.emptyList(),
            Collections.emptyList()),

    // Interazioni con ATTERRATO (fisica, non elementale)
    // + CONTUNDENTE
    /** Se il difensore è {@code ATTERRATO} e riceve un danno {@code CONTUNDENTE}: il danno si moltiplica per 2; riceve {@code ATTERRATO}. */
    SCHIACCIAMENTO(
            Collections.singletonList(TipoEffettoDiStato.ATTERRATO),
            Collections.singletonList(TipoDanno.CONTUNDENTE),
            2.0d,
            Collections.emptyList(),
            Collections.singletonList(TipoEffettoDiStato.ATTERRATO)),

    // Interazioni con RALLENTATO
    // + TERRA / CONTUNDENTE
    /** Se il difensore è {@code RALLENTATO} e riceve un danno {@code TERRA} o {@code CONTUNDENTE}: il danno si moltiplica per 1,3; riceve {@code ATTERRATO}. */
    INCIAMPO(
            Collections.singletonList(TipoEffettoDiStato.RALLENTATO),
            Arrays.asList(TipoDanno.TERRA, TipoDanno.CONTUNDENTE),
            1.3d,
            Collections.emptyList(),
            Collections.singletonList(TipoEffettoDiStato.ATTERRATO)),

    // Interazioni con IMMOBILIZZATO
    // + FUOCO
    /** Se il difensore è {@code IMMOBILIZZATO} e riceve un danno {@code FUOCO}: il danno si moltiplica per 1,5; perde {@code IMMOBILIZZATO}. */
    INCENDIO_LIBERATORIO(
            Collections.singletonList(TipoEffettoDiStato.IMMOBILIZZATO),
            Collections.singletonList(TipoDanno.FUOCO),
            1.5d,
            Collections.singletonList(TipoEffettoDiStato.IMMOBILIZZATO),
            Collections.emptyList()),
    // + CONTUNDENTE / TERRA
    /** Se il difensore è {@code IMMOBILIZZATO} e riceve un danno {@code CONTUNDENTE} o {@code TERRA}: il danno si moltiplica per 1,3. */
    IMPATTO_RIGIDO(
            Collections.singletonList(TipoEffettoDiStato.IMMOBILIZZATO),
            Arrays.asList(TipoDanno.CONTUNDENTE, TipoDanno.TERRA),
            1.3d,
            Collections.emptyList(),
            Collections.emptyList()),

    // Interazioni con SPAVENTATO / CONFUSO
    // + ARCANO
    /** Se il difensore è {@code SPAVENTATO} o {@code CONFUSO} e riceve un danno {@code ARCANO}: perde {@code SPAVENTATO} e {@code CONFUSO}; riceve {@code STORDITO}. */
    SOVRACCARICO_MENTALE(
            Arrays.asList(TipoEffettoDiStato.SPAVENTATO, TipoEffettoDiStato.CONFUSO),
            Collections.singletonList(TipoDanno.ARCANO),
            1.0d,
            Arrays.asList(TipoEffettoDiStato.SPAVENTATO, TipoEffettoDiStato.CONFUSO),
            Collections.singletonList(TipoEffettoDiStato.STORDITO)),
    // + danno FISICO (qualsiasi tipo)
    /** Se il difensore è {@code SPAVENTATO} o {@code CONFUSO} e riceve un danno {@code TAGLIENTE}, {@code PERFORANTE} o {@code CONTUNDENTE}: il danno si moltiplica per 1,2; perde {@code SPAVENTATO} e {@code CONFUSO}. */
    SHOCK_DI_REALTA(
            Arrays.asList(TipoEffettoDiStato.SPAVENTATO, TipoEffettoDiStato.CONFUSO),
            Arrays.asList(TipoDanno.TAGLIENTE, TipoDanno.PERFORANTE, TipoDanno.CONTUNDENTE),
            1.2d,
            Arrays.asList(TipoEffettoDiStato.SPAVENTATO, TipoEffettoDiStato.CONFUSO),
            Collections.emptyList()),

    // Interazioni con CONFUSO (esclusiva, non condivisa con SPAVENTATO)
    // + VUOTO
    /** Se il difensore è {@code CONFUSO} e riceve un danno {@code VUOTO}: il danno si moltiplica per 1,5; perde {@code CONFUSO}; riceve {@code MENTE_FRATTURATA}. */
    FOLLIA_COSMICA(
            Collections.singletonList(TipoEffettoDiStato.CONFUSO),
            Collections.singletonList(TipoDanno.VUOTO),
            1.5d,
            Collections.singletonList(TipoEffettoDiStato.CONFUSO),
            Collections.singletonList(TipoEffettoDiStato.MENTE_FRATTURATA)),

    // Interazioni con ACCECATO
    // + ARIA
    /** Se il difensore è {@code ACCECATO} e riceve un danno {@code ARIA}: il danno si moltiplica per 1,05; perde {@code ACCECATO}. */
    DISPERSIONE(
            Collections.singletonList(TipoEffettoDiStato.ACCECATO),
            Collections.singletonList(TipoDanno.ARIA),
            1.05d,
            Collections.singletonList(TipoEffettoDiStato.ACCECATO),
            Collections.emptyList()),
    // + ACQUA
    /** Se il difensore è {@code ACCECATO} e riceve un danno {@code ACQUA}: il danno si moltiplica per 1,05; riceve {@code RALLENTATO}. */
    FANGO(
            Collections.singletonList(TipoEffettoDiStato.ACCECATO),
            Collections.singletonList(TipoDanno.ACQUA),
            1.05d,
            Collections.emptyList(),
            Collections.singletonList(TipoEffettoDiStato.RALLENTATO)),

    // Interazioni con ASSORDATO
    // + SONICO
    /** Se il difensore è {@code ASSORDATO} e riceve un danno {@code SONICO}: il danno si moltiplica per 1,05; perde {@code ASSORDATO}; riceve {@code STORDITO}. */
    DISORIENTAMENTO(
            Collections.singletonList(TipoEffettoDiStato.ASSORDATO),
            Collections.singletonList(TipoDanno.SONICO),
            1.05d,
            Collections.singletonList(TipoEffettoDiStato.ASSORDATO),
            Collections.singletonList(TipoEffettoDiStato.STORDITO)),

    // Interazioni con SILENZIATO
    // + ARCANO
    /** Se il difensore è {@code SILENZIATO} e riceve un danno {@code ARCANO}: nessun cambiamento di stato né di danno. */
    RISONANZA_SIGILLATA(
            Collections.singletonList(TipoEffettoDiStato.SILENZIATO),
            Collections.singletonList(TipoDanno.ARCANO),
            1.0d,
            Collections.emptyList(),
            Collections.emptyList()),
    // + PSICHICO
    /** Se il difensore è {@code SILENZIATO} e riceve un danno {@code PSICHICO}: il danno si moltiplica per 1,5; riceve {@code ACCECATO}. */
    ISOLAMENTO_SENSORIALE(
            Collections.singletonList(TipoEffettoDiStato.SILENZIATO),
            Collections.singletonList(TipoDanno.PSICHICO),
            1.5d,
            Collections.emptyList(),
            Collections.singletonList(TipoEffettoDiStato.ACCECATO));

    private static Set<TipoEffettoDiStato> insieme(Collection<TipoEffettoDiStato> stati) {
        return stati.isEmpty() ? EnumSet.noneOf(TipoEffettoDiStato.class) : EnumSet.copyOf(stati);
    }

    private final Collection<TipoEffettoDiStato> precondizioni;
    private final Collection<TipoDanno> innescanti;
    private final double moltiplicatore;
    // Le conseguenze sugli stati del difensore quando la reazione scatta (vedi interazioni_effetti_di_stato.md)
    private final Set<TipoEffettoDiStato> statiRimossi;
    private final Set<TipoEffettoDiStato> statiApplicati;

    /**
     * @param precondizioni gli stati del difensore di cui ne basta uno perché la reazione scatti
     * @param innescanti    i tipi di danno che la fanno scattare
     * @param moltiplicatore per quanto si moltiplica il danno (1 se non cambia)
     * @param tolti         gli stati che toglie al difensore quando scatta
     * @param aggiunti      gli stati che gli mette quando scatta
     */
    TipoInterazioneConEffettiDiStato(Collection<TipoEffettoDiStato> precondizioni, Collection<TipoDanno> innescanti,
                                     double moltiplicatore, Collection<TipoEffettoDiStato> tolti,
                                     Collection<TipoEffettoDiStato> aggiunti) {
        this.precondizioni = Collections.unmodifiableList(new ArrayList<>(precondizioni));
        this.innescanti = Collections.unmodifiableList(new ArrayList<>(innescanti));
        this.moltiplicatore = moltiplicatore;
        this.statiRimossi = Collections.unmodifiableSet(insieme(tolti));
        this.statiApplicati = Collections.unmodifiableSet(insieme(aggiunti));
    }

    /**
     * Gli effetti di stato necessari (ne basta uno) perché la reazione si inneschi.
     */
    public Collection<TipoEffettoDiStato> getPrecondizioni() {
        return precondizioni;
    }

    /**
     * I tipi di danno necessari perché la reazione si inneschi.
     */
    public Collection<TipoDanno> getInnescanti() {
        return innescanti;
    }

    /**
     * Per quanto si moltiplica il danno quando l'interazione scatta: più di 1 per una debolezza (il fulmine su un
     * BAGNATO, 1,5), meno di 1 per una resistenza (il fuoco su un BAGNATO, 0,5), 1 se non cambia il danno. I bonus che non
     * sono un moltiplicatore (il danno puro della RISONANZA_SIGILLATA, la difesa del guscio di ghiaccio, il critico
     * automatico del COLPO_DI_GRAZIA e della FRANTUMAZIONE_DEL_GHIACCIO) restano nel calcolo del danno.
     */
    public double getMoltiplicatore() {
        return moltiplicatore;
    }

    /**
     * Gli stati che la reazione, quando scatta, toglie al difensore (se li ha).
     * Si tolgono dopo aver messo quelli nuovi di tutto il colpo:
     * uno stato messo da una reazione e tolto da un'altra non resta.
     */
    public Set<TipoEffettoDiStato> getStatiRimossi() {
        return statiRimossi;
    }

    /**
     * Gli stati che la reazione, quando scatta, mette al difensore.
     */
    public Set<TipoEffettoDiStato> getStatiApplicati() {
        return statiApplicati;
    }

    /**
     * Il nome della reazione, nella lingua scelta.
     */
    public String getDescrizione() {
        return Testi.testoOCostante(this, "DESCRIZIONE");
    }

    /**
     * Se la reazione rende il colpo un critico automatico (×2, che non si somma a un critico vero):
     * il COLPO_DI_GRAZIA su chi è a terra o stordito e la FRANTUMAZIONE_DEL_GHIACCIO.
     */
    public boolean isCriticoAutomatico() {
        return this == COLPO_DI_GRAZIA || this == FRANTUMAZIONE_DEL_GHIACCIO;
    }

    /**
     * Il tipo di danno con cui la reazione investe anche i vicini del bersaglio nella fila del suo gruppo,
     * o null se resta sul bersaglio: l'ESPLOSIONE_DI_GAS il FUOCO, il RIGETTO il SONICO, l'ELETTROCUZIONE il FULMINE
     * (questo però si applica solo ai vicini con stato BAGNATO, inteso come nella stessa pozza).
     * Quanto e come lo dice il calcolo del danno (CalcolatoreCombattimento.calcolaDannoAdArea).
     */
    public TipoDanno getDannoAdArea() {
        switch (this) {
            case ESPLOSIONE_DI_GAS:
                return TipoDanno.FUOCO;
            case RIGETTO:
                return TipoDanno.SONICO;
            case ELETTROCUZIONE:
                return TipoDanno.FULMINE;
            default:
                return null;
        }
    }

    /**
     * La frase con cui il danno ad area investe un vicino ("L'esplosione investe"), seguita dal nome del vicino.
     */
    public String getTestoAdArea() {
        return Testi.testoOCostante(this, "AREA");
    }
}
