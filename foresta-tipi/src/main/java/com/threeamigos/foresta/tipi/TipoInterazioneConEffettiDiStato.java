package com.threeamigos.foresta.tipi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Le reazioni fra un tipo di danno e uno stato del difensore: una reazione scatta se il difensore ha almeno uno degli stati
 * delle precondizioni e riceve uno dei tipi di danno degli innescanti (vedi CalcolatoreCombattimento.interazioniAttive, che
 * le prova in sequenza). <b>L'ordine di dichiarazione è l'ordine in cui le reazioni si applicano</b>, per stato. (Le debolezze
 * e le resistenze della classe a un tipo di danno non sono qui: vedi TipoPersonaggio.getMoltiplicatoreDannoSubito.)
 * <p>
 * I testi di ogni costante (DESCRIZIONE) non stanno qui ma nel file {@code tipo_interazione_con_effetti_di_stato_<lingua>.properties}, alla voce
 * {@code COSTANTE.VOCE}, in modo che il gioco si possa tradurre (vedi {@link Testi}).
 *
 * @author Stefano Reksten
 */
public enum TipoInterazioneConEffettiDiStato {

    // Interazioni con BAGNATO
    // + FULMINE
    ELETTROCUZIONE(TipoEffettoDiStato.BAGNATO, TipoDanno.FULMINE, 1.5d),
    // + GELO
    CONGELAMENTO(TipoEffettoDiStato.BAGNATO, TipoDanno.GELO),
    // + FUOCO
    VAPORIZZAZIONE(TipoEffettoDiStato.BAGNATO, TipoDanno.FUOCO, 0.5d),

    // Interazioni con BRUCIATO
    // + ARIA
    ALIMENTAZIONE_FIAMMA(TipoEffettoDiStato.BRUCIATO, TipoDanno.ARIA, 1.5d),
    // + ACQUA
    ESTINZIONE(TipoEffettoDiStato.BRUCIATO, TipoDanno.ACQUA),
    // + GELO
    SCIOGLIMENTO_TERMICO(TipoEffettoDiStato.BRUCIATO, TipoDanno.GELO, 1.5d),
    // + VELENO
    ESPLOSIONE_DI_GAS(TipoEffettoDiStato.BRUCIATO, TipoDanno.VELENO, 1.3d),

    // Interazioni con AVVELENATO
    // + FUOCO
    // Il veleno si incendia: rimuove AVVELENATO, applica BRUCIATO, piccolo bonus di danno (effetto simile a Esplosione di gas, ma nel verso opposto)
    VAMPATA_TOSSICA(TipoEffettoDiStato.AVVELENATO, TipoDanno.FUOCO, 1.3d),
    // + ACIDO
    // +15% danno (non gestiamo corrosione/usura dell'armatura, vedi nota nel roadmap)
    REAZIONE_TOSSICA(TipoEffettoDiStato.AVVELENATO, TipoDanno.ACIDO, 1.15d),

    // Interazioni con SANGUINAMENTO
    // + ACQUA
    // Riduce il danno periodico da sanguinamento, piccolo bonus di danno
    DILUIZIONE_EMATICA(TipoEffettoDiStato.SANGUINAMENTO, TipoDanno.ACQUA, 1.05d),
    // + GELO
    // Rimuove SANGUINAMENTO, applica RALLENTATO, piccolo bonus di danno
    COAGULAZIONE_FORZATA(TipoEffettoDiStato.SANGUINAMENTO, TipoDanno.GELO, 1.05d),

    // Interazioni con CONGELATO
    // + CONTUNDENTE
    FRANTUMAZIONE_DEL_GHIACCIO(TipoEffettoDiStato.CONGELATO, TipoDanno.CONTUNDENTE, 2.0d),
    // + FUOCO
    DISGELO_VIOLENTO(TipoEffettoDiStato.CONGELATO, TipoDanno.FUOCO, 1.5d),
    // + FULMINE
    SUPERCONDUZIONE(TipoEffettoDiStato.CONGELATO, TipoDanno.FULMINE, 1.3d),

    // Interazioni con MALEDETTO
    // + NECROTICO
    MIETITURA(TipoEffettoDiStato.MALEDETTO, TipoDanno.NECROTICO),
    // + SACRO
    RIGETTO(TipoEffettoDiStato.MALEDETTO, TipoDanno.SACRO),
    // + VUOTO
    // Rimuove MALEDETTO, brucia il 25% dei MP massimi e infligge un danno pari agli MP persi
    COLLASSO_ENTROPICO(TipoEffettoDiStato.MALEDETTO, TipoDanno.VUOTO),

    // Interazioni con INFETTATO
    // + SACRO
    PURIFICAZIONE(TipoEffettoDiStato.INFETTATO, TipoDanno.SACRO, 1.5d),
    // + ARCANO
    SIFONE_VITALE(TipoEffettoDiStato.INFETTATO, TipoDanno.ARCANO),
    // + VELENO
    TOSSICITA_SETTICA(TipoEffettoDiStato.INFETTATO, TipoDanno.VELENO),

    // Interazioni con STORDITO / ATTERRATO (fisica, non elementale)
    // + TAGLIENTE / PERFORANTE
    // NON uccide il personaggio ma infligge automaticamente un critico a 2x il danno normale
    COLPO_DI_GRAZIA(Arrays.asList(TipoEffettoDiStato.STORDITO, TipoEffettoDiStato.ATTERRATO), Arrays.asList(TipoDanno.TAGLIENTE, TipoDanno.PERFORANTE)),

    // Interazioni con ATTERRATO (fisica, non elementale)
    // + CONTUNDENTE
    // Raddoppia il danno fisico e ne estende l'atterramento
    SCHIACCIAMENTO(TipoEffettoDiStato.ATTERRATO, TipoDanno.CONTUNDENTE, 2.0d),

    // Interazioni con RALLENTATO
    // + TERRA / CONTUNDENTE
    // Il bersaglio non riesce a scartare l'impatto: +30% danno e viene ATTERRATO
    INCIAMPO(Collections.singletonList(TipoEffettoDiStato.RALLENTATO), Arrays.asList(TipoDanno.TERRA, TipoDanno.CONTUNDENTE), 1.3d),

    // Interazioni con IMMOBILIZZATO
    // + FUOCO
    // Il fuoco distrugge all'istante l'immobilizzazione: rimuove IMMOBILIZZATO, +50% danno
    INCENDIO_LIBERATORIO(TipoEffettoDiStato.IMMOBILIZZATO, TipoDanno.FUOCO, 1.5d),
    // + CONTUNDENTE / TERRA
    // Il bersaglio non può assecondare il colpo muovendo le gambe: +30% danno
    IMPATTO_RIGIDO(Collections.singletonList(TipoEffettoDiStato.IMMOBILIZZATO), Arrays.asList(TipoDanno.CONTUNDENTE, TipoDanno.TERRA), 1.3d),

    // Interazioni con SPAVENTATO / CONFUSO
    // + ARCANO
    // Trasforma lo stato in uno STORDITO pesante (1-2 turni)
    SOVRACCARICO_MENTALE(Arrays.asList(TipoEffettoDiStato.SPAVENTATO, TipoEffettoDiStato.CONFUSO), Collections.singletonList(TipoDanno.ARCANO)),
    // + danno FISICO (qualsiasi tipo)
    // Il dolore acuto interrompe subito lo stato e infligge il 20% di danno in più
    SHOCK_DI_REALTA(Arrays.asList(TipoEffettoDiStato.SPAVENTATO, TipoEffettoDiStato.CONFUSO), Arrays.asList(TipoDanno.TAGLIENTE, TipoDanno.PERFORANTE, TipoDanno.CONTUNDENTE), 1.2d),

    // Interazioni con CONFUSO (esclusiva, non condivisa con SPAVENTATO)
    // + VUOTO
    // Rimuove CONFUSO, +50% danno, applica MENTE_FRATTURATA
    FOLLIA_COSMICA(TipoEffettoDiStato.CONFUSO, TipoDanno.VUOTO, 1.5d),

    // Interazioni con ACCECATO
    // + ARIA
    // Rimuove ACCECATO, piccolo bonus di danno
    DISPERSIONE(TipoEffettoDiStato.ACCECATO, TipoDanno.ARIA, 1.05d),
    // + ACQUA
    // Applica RALLENTATO severo, piccolo bonus di danno
    FANGO(TipoEffettoDiStato.ACCECATO, TipoDanno.ACQUA, 1.05d),

    // Interazioni con ASSORDATO
    // + SONICO
    // Trasforma in STORDITO di 1 turno, piccolo bonus di danno
    DISORIENTAMENTO(TipoEffettoDiStato.ASSORDATO, TipoDanno.SONICO, 1.05d),

    // Interazioni con SILENZIATO
    // + ARCANO
    // +30% danno Puro che ignora le difese
    RISONANZA_SIGILLATA(TipoEffettoDiStato.SILENZIATO, TipoDanno.ARCANO),
    // + PSICHICO
    // +50% danno, estende anche ACCECATO
    ISOLAMENTO_SENSORIALE(TipoEffettoDiStato.SILENZIATO, TipoDanno.PSICHICO, 1.5d);

    private final Collection<TipoEffettoDiStato> precondizioni;
    private final Collection<TipoDanno> innescanti;
    private final double moltiplicatore;

    TipoInterazioneConEffettiDiStato(TipoEffettoDiStato precondizione, TipoDanno innescante) {
        this(precondizione, innescante, 1.0d);
    }

    TipoInterazioneConEffettiDiStato(TipoEffettoDiStato precondizione, TipoDanno innescante, double moltiplicatore) {
        this(Collections.singleton(precondizione), Collections.singleton(innescante), moltiplicatore);
    }

    TipoInterazioneConEffettiDiStato(Collection<TipoEffettoDiStato> precondizioni, Collection<TipoDanno> innescanti) {
        this(precondizioni, innescanti, 1.0d);
    }

    TipoInterazioneConEffettiDiStato(Collection<TipoEffettoDiStato> precondizioni, Collection<TipoDanno> innescanti, double moltiplicatore) {
        this.precondizioni = precondizioni;
        this.innescanti = innescanti;
        this.moltiplicatore = moltiplicatore;
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
     * BAGNATO, 1,5), meno di 1 per una resistenza (il fuoco su un BAGNATO, 0,5), 1 se non cambia il danno. Il solo
     * moltiplicatore che non sta qui è quello della MIETITURA, che dipende dalla SAGGEZZA del difensore
     * (CalcolatoreCombattimento.moltiplicatoreMietitura); i bonus che non sono un moltiplicatore (il danno puro della
     * RISONANZA_SIGILLATA, la difesa del guscio di ghiaccio) restano nel calcolo del danno.
     */
    public double getMoltiplicatore() {
        return moltiplicatore;
    }
}
