package com.threeamigos.foresta.tipi;

import com.threeamigos.foresta.testi.Testi;
import java.util.*;

import static com.threeamigos.foresta.tipi.TipoDanno.*;
import static com.threeamigos.foresta.tipi.TipoEffettoDiStato.*;

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

    // Ogni reazione ha cinque dati, in quest'ordine: gli stati del difensore che la permettono (ne basta uno), i tipi di
    // danno che la fanno scattare, il moltiplicatore del danno (1.0 lo lascia com'è), gli stati che toglie al difensore e
    // quelli che gli mette (nessuno() se non ne toglie o non ne mette). I nomi degli stati e dei danni sono quelli di
    // TipoEffettoDiStato e di TipoDanno (vedi gli import statici).

    // Interazioni con BAGNATO
    // + FULMINE
    /** Se il difensore è {@code BAGNATO} e riceve un danno {@code FULMINE}: il danno si moltiplica per 1,5. */
    ELETTROCUZIONE(stati(BAGNATO), danni(FULMINE), 1.5d, nessuno(), nessuno()),
    // + GELO
    /** Se il difensore è {@code BAGNATO} e riceve un danno {@code GELO}: perde {@code BAGNATO}; riceve {@code CONGELATO}. */
    CONGELAMENTO(stati(BAGNATO), danni(GELO), 1.0d, stati(BAGNATO), stati(CONGELATO)),
    // + FUOCO
    /** Se il difensore è {@code BAGNATO} e riceve un danno {@code FUOCO}: il danno si moltiplica per 0,5; perde {@code BAGNATO}; riceve {@code ACCECATO}. */
    VAPORIZZAZIONE(stati(BAGNATO), danni(FUOCO), 0.5d, stati(BAGNATO), stati(ACCECATO)),

    // + ARIA
    /** Se il difensore è {@code BAGNATO} e riceve un danno {@code ARIA}: perde {@code BAGNATO}; riceve {@code RAFFREDDATO} (tre turni, e a ogni turno una volta su due starnutisce e perde il turno). */
    RAFFREDDAMENTO(stati(BAGNATO), danni(ARIA), 1.0d, stati(BAGNATO), stati(RAFFREDDATO)),

    // Interazioni con BRUCIATO
    // + ARIA
    /** Se il difensore è {@code BRUCIATO} e riceve un danno {@code ARIA}: il danno si moltiplica per 1,5; riceve {@code BRUCIATO}. */
    ALIMENTAZIONE_FIAMMA(stati(BRUCIATO), danni(ARIA), 1.5d, nessuno(), stati(BRUCIATO)),
    // + ACQUA
    /** Se il difensore è {@code BRUCIATO} e riceve un danno {@code ACQUA}: perde {@code BRUCIATO} e {@code BAGNATO}. */
    ESTINZIONE(stati(BRUCIATO), danni(ACQUA), 1.0d, stati(BRUCIATO, BAGNATO), nessuno()),
    // + GELO
    /** Se il difensore è {@code BRUCIATO} e riceve un danno {@code GELO}: il danno si moltiplica per 1,5; perde {@code BRUCIATO}; riceve {@code BAGNATO}. */
    SCIOGLIMENTO_TERMICO(stati(BRUCIATO), danni(GELO), 1.5d, stati(BRUCIATO), stati(BAGNATO)),
    // + VELENO
    /** Se il difensore è {@code BRUCIATO} e riceve un danno {@code VELENO}: il danno si moltiplica per 1,3; perde {@code BRUCIATO} e {@code AVVELENATO}. */
    ESPLOSIONE_DI_GAS(stati(BRUCIATO), danni(VELENO), 1.3d, stati(BRUCIATO, AVVELENATO), nessuno()),

    // Interazioni con AVVELENATO
    // + FUOCO
    /** Se il difensore è {@code AVVELENATO} e riceve un danno {@code FUOCO}: il danno si moltiplica per 1,3; perde {@code AVVELENATO}; riceve {@code BRUCIATO}. */
    VAMPATA_TOSSICA(stati(AVVELENATO), danni(FUOCO), 1.3d, stati(AVVELENATO), stati(BRUCIATO)),
    // + ACIDO
    /** Se il difensore è {@code AVVELENATO} e riceve un danno {@code ACIDO}: il danno si moltiplica per 1,15. */
    REAZIONE_TOSSICA(stati(AVVELENATO), danni(ACIDO), 1.15d, nessuno(), nessuno()),

    // Interazioni con SANGUINAMENTO
    // + ACQUA
    /** Se il difensore è {@code SANGUINAMENTO} e riceve un danno {@code ACQUA}: il danno si moltiplica per 1,05. */
    DILUIZIONE_EMATICA(stati(SANGUINAMENTO), danni(ACQUA), 1.05d, nessuno(), nessuno()),
    // + GELO
    /** Se il difensore è {@code SANGUINAMENTO} e riceve un danno {@code GELO}: il danno si moltiplica per 1,05; perde {@code SANGUINAMENTO}; riceve {@code RALLENTATO}. */
    COAGULAZIONE_FORZATA(stati(SANGUINAMENTO), danni(GELO), 1.05d, stati(SANGUINAMENTO), stati(RALLENTATO)),

    // Interazioni con CONGELATO
    // + CONTUNDENTE
    /** Se il difensore è {@code CONGELATO} e riceve un danno {@code CONTUNDENTE}: perde {@code CONGELATO}. */
    FRANTUMAZIONE_DEL_GHIACCIO(stati(CONGELATO), danni(CONTUNDENTE), 1.0d, stati(CONGELATO), nessuno()),
    // + FUOCO
    /** Se il difensore è {@code CONGELATO} e riceve un danno {@code FUOCO}: il danno si moltiplica per 1,5; perde {@code CONGELATO}. */
    DISGELO_VIOLENTO(stati(CONGELATO), danni(FUOCO), 1.5d, stati(CONGELATO), nessuno()),
    // + FULMINE
    /** Se il difensore è {@code CONGELATO} e riceve un danno {@code FULMINE}: il danno si moltiplica per 1,3. */
    SUPERCONDUZIONE(stati(CONGELATO), danni(FULMINE), 1.3d, nessuno(), nessuno()),

    // Interazioni con MALEDETTO
    // + NECROTICO
    /** Se il difensore è {@code MALEDETTO} e riceve un danno {@code NECROTICO}: il danno si moltiplica per 2. */
    MIETITURA(stati(MALEDETTO), danni(NECROTICO), 2.0d, nessuno(), nessuno()),
    // + SACRO
    /** Se il difensore è {@code MALEDETTO} e riceve un danno {@code SACRO}: perde {@code MALEDETTO}. */
    RIGETTO(stati(MALEDETTO), danni(SACRO), 1.0d, stati(MALEDETTO), nessuno()),
    // + VUOTO
    /** Se il difensore è {@code MALEDETTO} e riceve un danno {@code VUOTO}: perde {@code MALEDETTO}. */
    COLLASSO_ENTROPICO(stati(MALEDETTO), danni(VUOTO), 1.0d, stati(MALEDETTO), nessuno()),

    // Interazioni con INFETTATO
    // + SACRO
    /** Se il difensore è {@code INFETTATO} e riceve un danno {@code SACRO}: il danno si moltiplica per 1,5; perde {@code INFETTATO}. */
    PURIFICAZIONE(stati(INFETTATO), danni(SACRO), 1.5d, stati(INFETTATO), nessuno()),
    // + ARCANO
    /** Se il difensore è {@code INFETTATO} e riceve un danno {@code ARCANO}: nessun cambiamento di stato né di danno. */
    SIFONE_VITALE(stati(INFETTATO), danni(ARCANO), 1.0d, nessuno(), nessuno()),
    // + VELENO
    /** Se il difensore è {@code INFETTATO} e riceve un danno {@code VELENO}: riceve {@code AVVELENATO}. */
    TOSSICITA_SETTICA(stati(INFETTATO), danni(VELENO), 1.2d, nessuno(), stati(AVVELENATO)),

    // Interazioni con STORDITO / ATTERRATO (fisica, non elementale)
    // + TAGLIENTE / PERFORANTE
    /** Se il difensore è {@code STORDITO} o {@code ATTERRATO} e riceve un danno {@code TAGLIENTE} o {@code PERFORANTE}: nessun cambiamento di stato né di danno. */
    COLPO_DI_GRAZIA(stati(STORDITO, ATTERRATO), danni(TAGLIENTE, PERFORANTE), 1.0d, nessuno(), nessuno()),

    // Interazioni con ATTERRATO (fisica, non elementale)
    // + CONTUNDENTE
    /** Se il difensore è {@code ATTERRATO} e riceve un danno {@code CONTUNDENTE}: il danno si moltiplica per 2; riceve {@code ATTERRATO}. */
    SCHIACCIAMENTO(stati(ATTERRATO), danni(CONTUNDENTE), 2.0d, nessuno(), stati(ATTERRATO)),

    // Interazioni con RALLENTATO
    // + TERRA / CONTUNDENTE
    /** Se il difensore è {@code RALLENTATO} e riceve un danno {@code TERRA} o {@code CONTUNDENTE}: il danno si moltiplica per 1,3; riceve {@code ATTERRATO}. */
    INCIAMPO(stati(RALLENTATO), danni(TERRA, CONTUNDENTE), 1.3d, nessuno(), stati(ATTERRATO)),

    // Interazioni con IMMOBILIZZATO
    // + FUOCO
    /** Se il difensore è {@code IMMOBILIZZATO} e riceve un danno {@code FUOCO}: il danno si moltiplica per 1,5; perde {@code IMMOBILIZZATO}. */
    INCENDIO_LIBERATORIO(stati(IMMOBILIZZATO), danni(FUOCO), 1.5d, stati(IMMOBILIZZATO), nessuno()),
    // + CONTUNDENTE / TERRA
    /** Se il difensore è {@code IMMOBILIZZATO} e riceve un danno {@code CONTUNDENTE} o {@code TERRA}: il danno si moltiplica per 1,3. */
    IMPATTO_RIGIDO(stati(IMMOBILIZZATO), danni(CONTUNDENTE, TERRA), 1.3d, nessuno(), nessuno()),

    // Interazioni con SPAVENTATO / CONFUSO
    // + ARCANO
    /** Se il difensore è {@code SPAVENTATO} o {@code CONFUSO} e riceve un danno {@code ARCANO}: perde {@code SPAVENTATO} e {@code CONFUSO}; riceve {@code STORDITO}. */
    SOVRACCARICO_MENTALE(stati(SPAVENTATO, CONFUSO), danni(ARCANO), 1.0d, stati(SPAVENTATO, CONFUSO), stati(STORDITO)),
    // + danno FISICO (qualsiasi tipo)
    /** Se il difensore è {@code SPAVENTATO} o {@code CONFUSO} e riceve un danno {@code TAGLIENTE}, {@code PERFORANTE} o {@code CONTUNDENTE}: il danno si moltiplica per 1,2; perde {@code SPAVENTATO} e {@code CONFUSO}. */
    SHOCK_DI_REALTA(stati(SPAVENTATO, CONFUSO), danni(TAGLIENTE, PERFORANTE, CONTUNDENTE), 1.2d, stati(SPAVENTATO, CONFUSO), nessuno()),

    // Interazioni con CONFUSO (esclusiva, non condivisa con SPAVENTATO)
    // + VUOTO
    /** Se il difensore è {@code CONFUSO} e riceve un danno {@code VUOTO}: il danno si moltiplica per 1,5; perde {@code CONFUSO}; riceve {@code MENTE_FRATTURATA}. */
    FOLLIA_COSMICA(stati(CONFUSO), danni(VUOTO), 1.5d, stati(CONFUSO), stati(MENTE_FRATTURATA)),

    // Interazioni con ACCECATO
    // + ARIA
    /** Se il difensore è {@code ACCECATO} e riceve un danno {@code ARIA}: il danno si moltiplica per 1,05; perde {@code ACCECATO}. */
    DISPERSIONE(stati(ACCECATO), danni(ARIA), 1.05d, stati(ACCECATO), nessuno()),
    // + ACQUA
    /** Se il difensore è {@code ACCECATO} e riceve un danno {@code ACQUA}: il danno si moltiplica per 1,05; riceve {@code RALLENTATO}. */
    FANGO(stati(ACCECATO), danni(ACQUA), 1.05d, nessuno(), stati(RALLENTATO)),

    // Interazioni con ASSORDATO
    // + SONICO
    /** Se il difensore è {@code ASSORDATO} e riceve un danno {@code SONICO}: il danno si moltiplica per 1,05; perde {@code ASSORDATO}; riceve {@code STORDITO}. */
    DISORIENTAMENTO(stati(ASSORDATO), danni(SONICO), 1.05d, stati(ASSORDATO), stati(STORDITO)),

    // Interazioni con SILENZIATO
    // + ARCANO
    /** Se il difensore è {@code SILENZIATO} e riceve un danno {@code ARCANO}: nessun cambiamento di stato né di danno. */
    RISONANZA_SIGILLATA(stati(SILENZIATO), danni(ARCANO), 1.0d, nessuno(), nessuno()),
    // + PSICHICO
    /** Se il difensore è {@code SILENZIATO} e riceve un danno {@code PSICHICO}: il danno si moltiplica per 1,5; riceve {@code ACCECATO}. */
    ISOLAMENTO_SENSORIALE(stati(SILENZIATO), danni(PSICHICO), 1.5d, nessuno(), stati(ACCECATO));

    // Per scrivere le reazioni qui sopra in una riga: gli stati, i danni, o nessuno
    private static List<TipoEffettoDiStato> stati(TipoEffettoDiStato... stati) {
        return Arrays.asList(stati);
    }

    private static List<TipoDanno> danni(TipoDanno... danni) {
        return Arrays.asList(danni);
    }

    private static <T> List<T> nessuno() {
        return Collections.emptyList();
    }

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
