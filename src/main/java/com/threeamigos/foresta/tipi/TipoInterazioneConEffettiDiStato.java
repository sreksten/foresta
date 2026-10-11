package com.threeamigos.foresta.tipi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Le reazioni fra un tipo di danno e uno stato del difensore: una reazione scatta se il difensore ha almeno uno degli stati
 * delle precondizioni e riceve uno dei tipi di danno degli innescanti (vedi CalcolatoreCombattimento.interazioniAttive, che
 * le prova in sequenza). <b>L'ordine di dichiarazione è l'ordine in cui le reazioni si applicano</b>, per stato; DANNO_VERO,
 * che dipende dalla classe del personaggio e non da uno stato, è l'ultima e si applica per prima.
 *
 * @author Stefano Reksten
 */
public enum TipoInterazioneConEffettiDiStato {

    // Interazioni con BAGNATO
    // + FULMINE
    ELETTROCUZIONE(TipoEffettoDiStato.BAGNATO, TipoDanno.FULMINE, "Elettrocuzione", 1.5d),
    // + GELO
    CONGELAMENTO(TipoEffettoDiStato.BAGNATO, TipoDanno.GELO, "Congelamento"),
    // + FUOCO
    VAPORIZZAZIONE(TipoEffettoDiStato.BAGNATO, TipoDanno.FUOCO, "Vaporizzazione", 0.5d),

    // Interazioni con BRUCIATO
    // + ARIA
    ALIMENTAZIONE_FIAMMA(TipoEffettoDiStato.BRUCIATO, TipoDanno.ARIA, "Alimentazione fiamma", 1.5d),
    // + ACQUA
    ESTINZIONE(TipoEffettoDiStato.BRUCIATO, TipoDanno.ACQUA, "Estinzione"),
    // + GELO
    SCIOGLIMENTO_TERMICO(TipoEffettoDiStato.BRUCIATO, TipoDanno.GELO, "Scioglimento termico", 1.5d),
    // + VELENO
    ESPLOSIONE_DI_GAS(TipoEffettoDiStato.BRUCIATO, TipoDanno.VELENO, "Esplosione di gas", 1.3d),

    // Interazioni con AVVELENATO
    // + FUOCO
    // Il veleno si incendia: rimuove AVVELENATO, applica BRUCIATO, piccolo bonus di danno (effetto simile a Esplosione di gas, ma nel verso opposto)
    VAMPATA_TOSSICA(TipoEffettoDiStato.AVVELENATO, TipoDanno.FUOCO, "Vampata Tossica", 1.3d),
    // + ACIDO
    // +15% danno (non gestiamo corrosione/usura dell'armatura, vedi nota nel roadmap)
    REAZIONE_TOSSICA(TipoEffettoDiStato.AVVELENATO, TipoDanno.ACIDO, "Reazione Tossica", 1.15d),

    // Interazioni con SANGUINAMENTO
    // + ACQUA
    // Riduce il danno periodico da sanguinamento, piccolo bonus di danno
    DILUIZIONE_EMATICA(TipoEffettoDiStato.SANGUINAMENTO, TipoDanno.ACQUA, "Diluizione Ematica", 1.05d),
    // + GELO
    // Rimuove SANGUINAMENTO, applica RALLENTATO, piccolo bonus di danno
    COAGULAZIONE_FORZATA(TipoEffettoDiStato.SANGUINAMENTO, TipoDanno.GELO, "Coagulazione Forzata", 1.05d),

    // Interazioni con CONGELATO
    // + CONTUNDENTE
    FRANTUMAZIONE_DEL_GHIACCIO(TipoEffettoDiStato.CONGELATO, TipoDanno.CONTUNDENTE, "Frantumazione del ghiaccio", 2.0d),
    // + FUOCO
    DISGELO_VIOLENTO(TipoEffettoDiStato.CONGELATO, TipoDanno.FUOCO, "Disgelo violento", 1.5d),
    // + FULMINE
    SUPERCONDUZIONE(TipoEffettoDiStato.CONGELATO, TipoDanno.FULMINE, "Superconduzione", 1.3d),

    // Interazioni con MALEDETTO
    // + NECROTICO
    MIETITURA(TipoEffettoDiStato.MALEDETTO, TipoDanno.NECROTICO, "Mietitura"),
    // + SACRO
    RIGETTO(TipoEffettoDiStato.MALEDETTO, TipoDanno.SACRO, "Rigetto"),
    // + VUOTO
    // Rimuove MALEDETTO, brucia il 25% dei MP massimi e infligge un danno pari agli MP persi
    COLLASSO_ENTROPICO(TipoEffettoDiStato.MALEDETTO, TipoDanno.VUOTO, "Collasso Entropico"),

    // Interazioni con INFETTATO
    // + SACRO
    PURIFICAZIONE(TipoEffettoDiStato.INFETTATO, TipoDanno.SACRO, "Purificazione", 1.5d),
    // + ARCANO
    SIFONE_VITALE(TipoEffettoDiStato.INFETTATO, TipoDanno.ARCANO, "Sifone Vitale"),
    // + VELENO
    TOSSICITA_SETTICA(TipoEffettoDiStato.INFETTATO, TipoDanno.VELENO, "Tossicità Settica"),

    // Interazioni con STORDITO / ATTERRATO (fisica, non elementale)
    // + TAGLIENTE / PERFORANTE
    // NON uccide il personaggio ma infligge automaticamente un critico a 2x il danno normale
    COLPO_DI_GRAZIA(Arrays.asList(TipoEffettoDiStato.STORDITO, TipoEffettoDiStato.ATTERRATO),
            Arrays.asList(TipoDanno.TAGLIENTE, TipoDanno.PERFORANTE),
            "Colpo di Grazia"),

    // Interazioni con ATTERRATO (fisica, non elementale)
    // + CONTUNDENTE
    // Raddoppia il danno fisico e ne estende l'atterramento
    SCHIACCIAMENTO(TipoEffettoDiStato.ATTERRATO, TipoDanno.CONTUNDENTE, "Schiacciamento", 2.0d),

    // Interazioni con RALLENTATO
    // + TERRA / CONTUNDENTE
    // Il bersaglio non riesce a scartare l'impatto: +30% danno e viene ATTERRATO
    INCIAMPO(Collections.singletonList(TipoEffettoDiStato.RALLENTATO),
            Arrays.asList(TipoDanno.TERRA, TipoDanno.CONTUNDENTE),
            "Inciampo", 1.3d),

    // Interazioni con IMMOBILIZZATO
    // + FUOCO
    // Il fuoco distrugge all'istante l'immobilizzazione: rimuove IMMOBILIZZATO, +50% danno
    INCENDIO_LIBERATORIO(TipoEffettoDiStato.IMMOBILIZZATO, TipoDanno.FUOCO, "Incendio Liberatorio", 1.5d),
    // + CONTUNDENTE / TERRA
    // Il bersaglio non può assecondare il colpo muovendo le gambe: +30% danno
    IMPATTO_RIGIDO(Collections.singletonList(TipoEffettoDiStato.IMMOBILIZZATO),
            Arrays.asList(TipoDanno.CONTUNDENTE, TipoDanno.TERRA),
            "Impatto Rigido", 1.3d),

    // Interazioni con SPAVENTATO / CONFUSO
    // + ARCANO
    // Trasforma lo stato in uno STORDITO pesante (1-2 turni)
    SOVRACCARICO_MENTALE(Arrays.asList(TipoEffettoDiStato.SPAVENTATO, TipoEffettoDiStato.CONFUSO),
            Collections.singletonList(TipoDanno.ARCANO),
            "Sovraccarico Mentale"),
    // + danno FISICO (qualsiasi tipo)
    // Il dolore acuto interrompe subito lo stato e infligge il 20% di danno in più
    SHOCK_DI_REALTA(Arrays.asList(TipoEffettoDiStato.SPAVENTATO, TipoEffettoDiStato.CONFUSO),
            Arrays.asList(TipoDanno.TAGLIENTE, TipoDanno.PERFORANTE, TipoDanno.CONTUNDENTE),
            "Shock di Realtà", 1.2d),

    // Interazioni con CONFUSO (esclusiva, non condivisa con SPAVENTATO)
    // + VUOTO
    // Rimuove CONFUSO, +50% danno, applica MENTE_FRATTURATA
    FOLLIA_COSMICA(TipoEffettoDiStato.CONFUSO, TipoDanno.VUOTO, "Follia Cosmica", 1.5d),

    // Interazioni con ACCECATO
    // + ARIA
    // Rimuove ACCECATO, piccolo bonus di danno
    DISPERSIONE(TipoEffettoDiStato.ACCECATO, TipoDanno.ARIA, "Dispersione", 1.05d),
    // + ACQUA
    // Applica RALLENTATO severo, piccolo bonus di danno
    FANGO(TipoEffettoDiStato.ACCECATO, TipoDanno.ACQUA, "Fango", 1.05d),

    // Interazioni con ASSORDATO
    // + SONICO
    // Trasforma in STORDITO di 1 turno, piccolo bonus di danno
    DISORIENTAMENTO(TipoEffettoDiStato.ASSORDATO, TipoDanno.SONICO, "Disorientamento", 1.05d),

    // Interazioni con SILENZIATO
    // + ARCANO
    // +30% danno Puro che ignora le difese
    RISONANZA_SIGILLATA(TipoEffettoDiStato.SILENZIATO, TipoDanno.ARCANO, "Risonanza Sigillata"),
    // + PSICHICO
    // +50% danno, estende anche ACCECATO
    ISOLAMENTO_SENSORIALE(TipoEffettoDiStato.SILENZIATO, TipoDanno.PSICHICO, "Isolamento Sensoriale", 1.5d),

    // Interazioni particolari che dipendono dalla classe del Personaggio
    // DANNO_VERO: la debolezza di una classe a un tipo di danno (oggi il SACRO sui non morti, vedi
    // PersonaggioBase.haInterazioneCon). Nonostante il nome non ignora la difesa: il danno, già ridotto dalla difesa, si
    // moltiplica per 2 (il "danno puro" che ignora la difesa è un'altra cosa: il bonus della RISONANZA_SIGILLATA).
    // Non ha stati né tipi di danno da controllare (liste vuote), quindi non scatta mai da solo nel ciclo di
    // CalcolatoreCombattimento.interazioniAttive, che lo aggiunge a parte.
    // Si dichiara per ultimo perché non dipende da uno stato, ma si applica per primo, come nel calcolo del danno di sempre;
    // l'ordine non cambia il risultato (moltiplica e basta, senza stati né dadi), cambia solo la posizione nella lista delle
    // interazioni.
    DANNO_VERO(Collections.emptyList(), Collections.emptyList(), "Danno Vero", 2.0d);

    private final Collection<TipoEffettoDiStato> precondizioni;
    private final Collection<TipoDanno> innescanti;
    private final String descrizione;
    private final double moltiplicatore;

    TipoInterazioneConEffettiDiStato(TipoEffettoDiStato precondizione, TipoDanno innescante, String descrizione) {
        this(precondizione, innescante, descrizione, 1.0d);
    }

    TipoInterazioneConEffettiDiStato(TipoEffettoDiStato precondizione, TipoDanno innescante, String descrizione, double moltiplicatore) {
        this(Collections.singleton(precondizione), Collections.singleton(innescante), descrizione, moltiplicatore);
    }

    TipoInterazioneConEffettiDiStato(Collection<TipoEffettoDiStato> precondizioni, Collection<TipoDanno> innescanti, String descrizione) {
        this(precondizioni, innescanti, descrizione, 1.0d);
    }

    TipoInterazioneConEffettiDiStato(Collection<TipoEffettoDiStato> precondizioni, Collection<TipoDanno> innescanti, String descrizione,
                                     double moltiplicatore) {
        this.precondizioni = precondizioni;
        this.innescanti = innescanti;
        this.descrizione = descrizione;
        this.moltiplicatore = moltiplicatore;
    }

    public Collection<TipoEffettoDiStato> getPrecondizioni() {
        return precondizioni;
    }

    public Collection<TipoDanno> getInnescanti() {
        return innescanti;
    }

    public String getDescrizione() {
        return descrizione;
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
