package com.threeamigos.foresta.motore.tipi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author Stefano Reksten
 */
public enum TipoInterazioneConEffettiDiStato {

    // Interazioni con BAGNATO
    // + FULMINE
    ELETTROCUZIONE(TipoEffettoDiStato.BAGNATO, TipoDanno.FULMINE, "Elettrocuzione"),
    // + GELO
    CONGELAMENTO(TipoEffettoDiStato.BAGNATO, TipoDanno.GELO, "Congelamento"),
    // + FUOCO
    VAPORIZZAZIONE(TipoEffettoDiStato.BAGNATO, TipoDanno.FUOCO, "Vaporizzazione"),

    // Interazioni con BRUCIATO
    // + ARIA
    ALIMENTAZIONE_FIAMMA(TipoEffettoDiStato.BRUCIATO, TipoDanno.ARIA, "Alimentazione fiamma"),
    // + ACQUA
    ESTINZIONE(TipoEffettoDiStato.BRUCIATO, TipoDanno.ACQUA, "Estinzione"),
    // + GELO
    SCIOGLIMENTO_TERMICO(TipoEffettoDiStato.BRUCIATO, TipoDanno.GELO, "Scioglimento termico"),
    // + VELENO
    ESPLOSIONE_DI_GAS(TipoEffettoDiStato.BRUCIATO, TipoDanno.VELENO, "Esplosione di gas"),

    // Interazioni con CONGELATO
    // + CONTUNDENTE
    FRANTUMAZIONE_DEL_GHIACCO(TipoEffettoDiStato.CONGELATO, TipoDanno.CONTUNDENTE, "Frantumazione del ghiaccio"),
    // + FUOCO
    DISGELO_VIOLENTO(TipoEffettoDiStato.CONGELATO, TipoDanno.FUOCO, "Disgelo violento"),
    // + FULMINE
    SUPERCONDUZIONE(TipoEffettoDiStato.CONGELATO, TipoDanno.FULMINE, "Superconduzione"),

    // Interazioni con MALEDETTO
    // + NECROTICO
    MIETITURA(TipoEffettoDiStato.MALEDETTO, TipoDanno.NECROTICO, "Mietitura"),
    // + SACRO
    RIGETTO(TipoEffettoDiStato.MALEDETTO, TipoDanno.SACRO, "Rigetto"),

    // Interazioni con INFETTATO
    // + SACRO
    PURIFICAZIONE(TipoEffettoDiStato.INFETTATO, TipoDanno.SACRO, "Purificazione"),
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
    SCHIACCIAMENTO(TipoEffettoDiStato.ATTERRATO, TipoDanno.CONTUNDENTE, "Schiacciamento"),

    // Interazioni con RALLENTATO
    // + TERRA / CONTUNDENTE
    // Il bersaglio non riesce a scartare l'impatto: +30% danno e viene ATTERRATO
    INCIAMPO(Collections.singletonList(TipoEffettoDiStato.RALLENTATO),
            Arrays.asList(TipoDanno.TERRA, TipoDanno.CONTUNDENTE),
            "Inciampo"),

    // Interazioni con SPAVENTATO
    // + ARCANO
    // Trasforma la paura in uno STORDITO pesante (1-2 turni)
    SOVRACCARICO_MENTALE(TipoEffettoDiStato.SPAVENTATO, TipoDanno.ARCANO, "Sovraccarico Mentale"),
    // + danno FISICO (qualsiasi tipo)
    // Il dolore acuto interrompe subito la paura e infligge il 20% di danno in più
    SHOCK_DI_REALTA(Collections.singletonList(TipoEffettoDiStato.SPAVENTATO),
            Arrays.asList(TipoDanno.TAGLIENTE, TipoDanno.PERFORANTE, TipoDanno.CONTUNDENTE),
            "Shock di Realtà"),

    // Interazioni con ACCECATO
    // + ARIA
    // Rimuove ACCECATO, piccolo bonus di danno
    DISPERSIONE(TipoEffettoDiStato.ACCECATO, TipoDanno.ARIA, "Dispersione"),
    // + ACQUA
    // Applica RALLENTATO severo, piccolo bonus di danno
    FANGO(TipoEffettoDiStato.ACCECATO, TipoDanno.ACQUA, "Fango"),

    // Interazioni con ASSORDATO
    // + SONICO
    // Trasforma in STORDITO di 1 turno, piccolo bonus di danno
    DISORIENTAMENTO(TipoEffettoDiStato.ASSORDATO, TipoDanno.SONICO, "Disorientamento"),

    // Interazioni con SILENZIATO
    // + ARCANO
    // +30% danno Puro che ignora le difese
    RISONANZA_SIGILLATA(TipoEffettoDiStato.SILENZIATO, TipoDanno.ARCANO, "Risonanza Sigillata"),
    // + PSICHICO
    // +50% danno, estende anche ACCECATO
    ISOLAMENTO_SENSORIALE(TipoEffettoDiStato.SILENZIATO, TipoDanno.PSICHICO, "Isolamento Sensoriale"),

    // Interazioni con AVVELENATO
    // + FUOCO
    // Il veleno si incendia: rimuove AVVELENATO, applica BRUCIATO, piccolo bonus di danno (effetto simile a Esplosione di gas, ma nel verso opposto)
    VAMPATA_TOSSICA(TipoEffettoDiStato.AVVELENATO, TipoDanno.FUOCO, "Vampata Tossica"),

    // Interazioni con SANGUINAMENTO
    // + ACQUA
    // Riduce il danno periodico da sanguinamento, piccolo bonus di danno
    DILUIZIONE_EMATICA(TipoEffettoDiStato.SANGUINAMENTO, TipoDanno.ACQUA, "Diluizione Ematica"),
    // + GELO
    // Rimuove SANGUINAMENTO, applica RALLENTATO, piccolo bonus di danno
    COAGULAZIONE_FORZATA(TipoEffettoDiStato.SANGUINAMENTO, TipoDanno.GELO, "Coagulazione Forzata");

    private final Collection<TipoEffettoDiStato> precondizioni;
    private final Collection<TipoDanno> innescanti;
    private final String descrizione;

    TipoInterazioneConEffettiDiStato(TipoEffettoDiStato precondizione, TipoDanno innescante, String descrizione) {
        this.precondizioni = Collections.singleton(precondizione);
        this.innescanti = Collections.singleton(innescante);
        this.descrizione = descrizione;
    }

    TipoInterazioneConEffettiDiStato(Collection<TipoEffettoDiStato> precondizioni, Collection<TipoDanno> innescanti, String descrizione) {
        this.precondizioni = precondizioni;
        this.innescanti = innescanti;
        this.descrizione = descrizione;
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
}
