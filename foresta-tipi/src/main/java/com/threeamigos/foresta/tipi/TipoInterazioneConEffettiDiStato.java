package com.threeamigos.foresta.tipi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

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
    ELETTROCUZIONE(
            Collections.singletonList(TipoEffettoDiStato.BAGNATO),
            Collections.singletonList(TipoDanno.FULMINE),
            1.5d,
            Collections.emptyList(),
            Collections.emptyList()),
    // + GELO
    CONGELAMENTO(
            Collections.singletonList(TipoEffettoDiStato.BAGNATO),
            Collections.singletonList(TipoDanno.GELO),
            1.0d,
            Collections.singletonList(TipoEffettoDiStato.BAGNATO),
            Collections.singletonList(TipoEffettoDiStato.CONGELATO)),
    // + FUOCO
    VAPORIZZAZIONE(
            Collections.singletonList(TipoEffettoDiStato.BAGNATO),
            Collections.singletonList(TipoDanno.FUOCO),
            0.5d,
            Collections.singletonList(TipoEffettoDiStato.BAGNATO),
            Collections.singletonList(TipoEffettoDiStato.ACCECATO)),

    // Interazioni con BRUCIATO
    // + ARIA
    ALIMENTAZIONE_FIAMMA(
            Collections.singletonList(TipoEffettoDiStato.BRUCIATO),
            Collections.singletonList(TipoDanno.ARIA),
            1.5d,
            Collections.emptyList(),
            Collections.singletonList(TipoEffettoDiStato.BRUCIATO)),
    // + ACQUA
    ESTINZIONE(
            Collections.singletonList(TipoEffettoDiStato.BRUCIATO),
            Collections.singletonList(TipoDanno.ACQUA),
            1.0d,
            Arrays.asList(TipoEffettoDiStato.BRUCIATO, TipoEffettoDiStato.BAGNATO),
            Collections.emptyList()),
    // + GELO
    SCIOGLIMENTO_TERMICO(
            Collections.singletonList(TipoEffettoDiStato.BRUCIATO),
            Collections.singletonList(TipoDanno.GELO),
            1.5d,
            Collections.singletonList(TipoEffettoDiStato.BRUCIATO),
            Collections.singletonList(TipoEffettoDiStato.BAGNATO)),
    // + VELENO
    ESPLOSIONE_DI_GAS(
            Collections.singletonList(TipoEffettoDiStato.BRUCIATO),
            Collections.singletonList(TipoDanno.VELENO),
            1.3d,
            Arrays.asList(TipoEffettoDiStato.BRUCIATO, TipoEffettoDiStato.AVVELENATO),
            Collections.emptyList()),

    // Interazioni con AVVELENATO
    // + FUOCO
    // Il veleno si incendia: rimuove AVVELENATO, applica BRUCIATO, piccolo bonus di danno (effetto simile a Esplosione di gas, ma nel verso opposto)
    VAMPATA_TOSSICA(
            Collections.singletonList(TipoEffettoDiStato.AVVELENATO),
            Collections.singletonList(TipoDanno.FUOCO),
            1.3d,
            Collections.singletonList(TipoEffettoDiStato.AVVELENATO),
            Collections.singletonList(TipoEffettoDiStato.BRUCIATO)),
    // + ACIDO
    // +15% danno (non gestiamo corrosione/usura dell'armatura, vedi nota nel roadmap)
    REAZIONE_TOSSICA(
            Collections.singletonList(TipoEffettoDiStato.AVVELENATO),
            Collections.singletonList(TipoDanno.ACIDO),
            1.15d,
            Collections.emptyList(),
            Collections.emptyList()),

    // Interazioni con SANGUINAMENTO
    // + ACQUA
    // Riduce il danno periodico da sanguinamento, piccolo bonus di danno
    DILUIZIONE_EMATICA(
            Collections.singletonList(TipoEffettoDiStato.SANGUINAMENTO),
            Collections.singletonList(TipoDanno.ACQUA),
            1.05d,
            Collections.emptyList(),
            Collections.emptyList()),
    // + GELO
    // Rimuove SANGUINAMENTO, applica RALLENTATO, piccolo bonus di danno
    COAGULAZIONE_FORZATA(
            Collections.singletonList(TipoEffettoDiStato.SANGUINAMENTO),
            Collections.singletonList(TipoDanno.GELO),
            1.05d,
            Collections.singletonList(TipoEffettoDiStato.SANGUINAMENTO),
            Collections.singletonList(TipoEffettoDiStato.RALLENTATO)),

    // Interazioni con CONGELATO
    // + CONTUNDENTE
    FRANTUMAZIONE_DEL_GHIACCIO(
            Collections.singletonList(TipoEffettoDiStato.CONGELATO),
            Collections.singletonList(TipoDanno.CONTUNDENTE),
            1.0d,
            Collections.singletonList(TipoEffettoDiStato.CONGELATO),
            Collections.emptyList()),
    // + FUOCO
    DISGELO_VIOLENTO(
            Collections.singletonList(TipoEffettoDiStato.CONGELATO),
            Collections.singletonList(TipoDanno.FUOCO),
            1.5d,
            Collections.singletonList(TipoEffettoDiStato.CONGELATO),
            Collections.emptyList()),
    // + FULMINE
    SUPERCONDUZIONE(
            Collections.singletonList(TipoEffettoDiStato.CONGELATO),
            Collections.singletonList(TipoDanno.FULMINE),
            1.3d,
            Collections.emptyList(),
            Collections.emptyList()),

    // Interazioni con MALEDETTO
    // + NECROTICO
    MIETITURA(
            Collections.singletonList(TipoEffettoDiStato.MALEDETTO),
            Collections.singletonList(TipoDanno.NECROTICO),
            2.0d,
            Collections.emptyList(),
            Collections.emptyList()),
    // + SACRO
    RIGETTO(
            Collections.singletonList(TipoEffettoDiStato.MALEDETTO),
            Collections.singletonList(TipoDanno.SACRO),
            1.0d,
            Collections.singletonList(TipoEffettoDiStato.MALEDETTO),
            Collections.emptyList()),
    // + VUOTO
    // Rimuove MALEDETTO, brucia il 25% dei MP massimi e infligge un danno pari agli MP persi
    COLLASSO_ENTROPICO(
            Collections.singletonList(TipoEffettoDiStato.MALEDETTO),
            Collections.singletonList(TipoDanno.VUOTO),
            1.0d,
            Collections.singletonList(TipoEffettoDiStato.MALEDETTO),
            Collections.emptyList()),

    // Interazioni con INFETTATO
    // + SACRO
    PURIFICAZIONE(
            Collections.singletonList(TipoEffettoDiStato.INFETTATO),
            Collections.singletonList(TipoDanno.SACRO),
            1.5d,
            Collections.singletonList(TipoEffettoDiStato.INFETTATO),
            Collections.emptyList()),
    // + ARCANO
    SIFONE_VITALE(
            Collections.singletonList(TipoEffettoDiStato.INFETTATO),
            Collections.singletonList(TipoDanno.ARCANO),
            1.0d,
            Collections.emptyList(),
            Collections.emptyList()),
    // + VELENO
    TOSSICITA_SETTICA(
            Collections.singletonList(TipoEffettoDiStato.INFETTATO),
            Collections.singletonList(TipoDanno.VELENO),
            1.0d,
            Collections.emptyList(),
            Collections.singletonList(TipoEffettoDiStato.AVVELENATO)),

    // Interazioni con STORDITO / ATTERRATO (fisica, non elementale)
    // + TAGLIENTE / PERFORANTE
    // NON uccide il personaggio ma infligge automaticamente un critico a 2x il danno normale
    COLPO_DI_GRAZIA(
            Arrays.asList(TipoEffettoDiStato.STORDITO, TipoEffettoDiStato.ATTERRATO),
            Arrays.asList(TipoDanno.TAGLIENTE, TipoDanno.PERFORANTE),
            1.0d,
            Collections.emptyList(),
            Collections.emptyList()),

    // Interazioni con ATTERRATO (fisica, non elementale)
    // + CONTUNDENTE
    // Raddoppia il danno fisico e ne estende l'atterramento
    SCHIACCIAMENTO(
            Collections.singletonList(TipoEffettoDiStato.ATTERRATO),
            Collections.singletonList(TipoDanno.CONTUNDENTE),
            2.0d,
            Collections.emptyList(),
            Collections.singletonList(TipoEffettoDiStato.ATTERRATO)),

    // Interazioni con RALLENTATO
    // + TERRA / CONTUNDENTE
    // Il bersaglio non riesce a scartare l'impatto: +30% danno e viene ATTERRATO
    INCIAMPO(
            Collections.singletonList(TipoEffettoDiStato.RALLENTATO),
            Arrays.asList(TipoDanno.TERRA, TipoDanno.CONTUNDENTE),
            1.3d,
            Collections.emptyList(),
            Collections.singletonList(TipoEffettoDiStato.ATTERRATO)),

    // Interazioni con IMMOBILIZZATO
    // + FUOCO
    // Il fuoco distrugge all'istante l'immobilizzazione: rimuove IMMOBILIZZATO, +50% danno
    INCENDIO_LIBERATORIO(
            Collections.singletonList(TipoEffettoDiStato.IMMOBILIZZATO),
            Collections.singletonList(TipoDanno.FUOCO),
            1.5d,
            Collections.singletonList(TipoEffettoDiStato.IMMOBILIZZATO),
            Collections.emptyList()),
    // + CONTUNDENTE / TERRA
    // Il bersaglio non può assecondare il colpo muovendo le gambe: +30% danno
    IMPATTO_RIGIDO(
            Collections.singletonList(TipoEffettoDiStato.IMMOBILIZZATO),
            Arrays.asList(TipoDanno.CONTUNDENTE, TipoDanno.TERRA),
            1.3d,
            Collections.emptyList(),
            Collections.emptyList()),

    // Interazioni con SPAVENTATO / CONFUSO
    // + ARCANO
    // Trasforma lo stato in uno STORDITO pesante (1-2 turni)
    SOVRACCARICO_MENTALE(
            Arrays.asList(TipoEffettoDiStato.SPAVENTATO, TipoEffettoDiStato.CONFUSO),
            Collections.singletonList(TipoDanno.ARCANO),
            1.0d,
            Arrays.asList(TipoEffettoDiStato.SPAVENTATO, TipoEffettoDiStato.CONFUSO),
            Collections.singletonList(TipoEffettoDiStato.STORDITO)),
    // + danno FISICO (qualsiasi tipo)
    // Il dolore acuto interrompe subito lo stato e infligge il 20% di danno in più
    SHOCK_DI_REALTA(
            Arrays.asList(TipoEffettoDiStato.SPAVENTATO, TipoEffettoDiStato.CONFUSO),
            Arrays.asList(TipoDanno.TAGLIENTE, TipoDanno.PERFORANTE, TipoDanno.CONTUNDENTE),
            1.2d,
            Arrays.asList(TipoEffettoDiStato.SPAVENTATO, TipoEffettoDiStato.CONFUSO),
            Collections.emptyList()),

    // Interazioni con CONFUSO (esclusiva, non condivisa con SPAVENTATO)
    // + VUOTO
    // Rimuove CONFUSO, +50% danno, applica MENTE_FRATTURATA
    FOLLIA_COSMICA(
            Collections.singletonList(TipoEffettoDiStato.CONFUSO),
            Collections.singletonList(TipoDanno.VUOTO),
            1.5d,
            Collections.singletonList(TipoEffettoDiStato.CONFUSO),
            Collections.singletonList(TipoEffettoDiStato.MENTE_FRATTURATA)),

    // Interazioni con ACCECATO
    // + ARIA
    // Rimuove ACCECATO, piccolo bonus di danno
    DISPERSIONE(
            Collections.singletonList(TipoEffettoDiStato.ACCECATO),
            Collections.singletonList(TipoDanno.ARIA),
            1.05d,
            Collections.singletonList(TipoEffettoDiStato.ACCECATO),
            Collections.emptyList()),
    // + ACQUA
    // Applica RALLENTATO severo, piccolo bonus di danno
    FANGO(
            Collections.singletonList(TipoEffettoDiStato.ACCECATO),
            Collections.singletonList(TipoDanno.ACQUA),
            1.05d,
            Collections.emptyList(),
            Collections.singletonList(TipoEffettoDiStato.RALLENTATO)),

    // Interazioni con ASSORDATO
    // + SONICO
    // Trasforma in STORDITO di 1 turno, piccolo bonus di danno
    DISORIENTAMENTO(
            Collections.singletonList(TipoEffettoDiStato.ASSORDATO),
            Collections.singletonList(TipoDanno.SONICO),
            1.05d,
            Collections.singletonList(TipoEffettoDiStato.ASSORDATO),
            Collections.singletonList(TipoEffettoDiStato.STORDITO)),

    // Interazioni con SILENZIATO
    // + ARCANO
    // +30% danno Puro che ignora le difese
    RISONANZA_SIGILLATA(
            Collections.singletonList(TipoEffettoDiStato.SILENZIATO),
            Collections.singletonList(TipoDanno.ARCANO),
            1.0d,
            Collections.emptyList(),
            Collections.emptyList()),
    // + PSICHICO
    // +50% danno, estende anche ACCECATO
    ISOLAMENTO_SENSORIALE(
            Collections.singletonList(TipoEffettoDiStato.SILENZIATO),
            Collections.singletonList(TipoDanno.PSICHICO),
            1.5d,
            Collections.emptyList(),
            Collections.singletonList(TipoEffettoDiStato.ACCECATO));

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

    private static Set<TipoEffettoDiStato> insieme(Collection<TipoEffettoDiStato> stati) {
        return stati.isEmpty() ? EnumSet.noneOf(TipoEffettoDiStato.class) : EnumSet.copyOf(stati);
    }

    /**
     * Gli stati che la reazione, quando scatta, toglie al difensore (se li ha). Si tolgono dopo aver messo quelli nuovi
     * di tutto il colpo: uno stato messo da una reazione e tolto da un'altra non resta.
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

    public Collection<TipoEffettoDiStato> getPrecondizioni() {
        return precondizioni;
    }

    public Collection<TipoDanno> getInnescanti() {
        return innescanti;
    }

    public String getDescrizione() {
        return Testi.testoOCostante(this, "DESCRIZIONE");
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
     * Se la reazione rende il colpo un critico automatico (×2, che non si somma a un critico vero): il COLPO_DI_GRAZIA su
     * chi è a terra o stordito e la FRANTUMAZIONE_DEL_GHIACCIO.
     */
    public boolean isCriticoAutomatico() {
        return this == COLPO_DI_GRAZIA || this == FRANTUMAZIONE_DEL_GHIACCIO;
    }
}
