package com.threeamigos.foresta.eventi.interni;

import com.threeamigos.foresta.eventi.EventoSuPersonaggio;
import com.threeamigos.foresta.eventi.TipoEvento;
import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.motore.modellodati.RisultatoValutazioneAttaccante;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 * Un Personaggio attaccante fa una valutazione del comportamento da assumere per attaccare
 * un Personaggio bersaglio.
 *
 * @author Stefano Reksten
 */
public class InternoRisultatoValutazionePersonaggioAttaccante extends EventoSuPersonaggio {

    private final Personaggio bersaglio;
    private final RisultatoValutazioneAttaccante valutazione;
    private final Incantesimo incantesimo;
    private final double probabilitaColpireFisico;
    private final double probabilitaColpireMagico;
    private final double possibiliDanniFisici;
    private final double possibiliDanniMagici;

    /**
     * @param personaggio il Personaggio che fa la valutazione
     * @param bersaglio il Personaggio bersaglio
     * @param valutazione il risultato della valutazione
     */
    public InternoRisultatoValutazionePersonaggioAttaccante(Personaggio personaggio, Personaggio bersaglio,
                                                            RisultatoValutazioneAttaccante valutazione) {
        super(TipoEvento.INTERNO_RISULTATO_VALUTAZIONE_PERSONAGGIO_ATTACCANTE, personaggio);
        this.bersaglio = bersaglio;
        this.valutazione = valutazione;
        this.incantesimo = null;
        probabilitaColpireFisico = -1;
        probabilitaColpireMagico = -1;
        possibiliDanniFisici = -1;
        possibiliDanniMagici = -1;
    }

    /**
     * @param personaggio il Personaggio che fa la valutazione
     * @param bersaglio il Personaggio bersaglio
     * @param valutazione il risultato della valutazione
     * @param incantesimo l'Incantesimo che viene utilizzato per attaccare
     */
    public InternoRisultatoValutazionePersonaggioAttaccante(Personaggio personaggio, Personaggio bersaglio,
                                                            RisultatoValutazioneAttaccante valutazione, Incantesimo incantesimo) {
        super(TipoEvento.INTERNO_RISULTATO_VALUTAZIONE_PERSONAGGIO_ATTACCANTE, personaggio);
        this.bersaglio = bersaglio;
        this.valutazione = valutazione;
        this.incantesimo = incantesimo;
        probabilitaColpireFisico = -1;
        probabilitaColpireMagico = -1;
        possibiliDanniFisici = -1;
        possibiliDanniMagici = -1;
    }

    /**
     * @param personaggio il Personaggio che fa la valutazione
     * @param bersaglio il Personaggio bersaglio
     * @param valutazione il risultato della valutazione
     * @param incantesimo l'Incantesimo che viene utilizzato per attaccare
     * @param probabilitaColpireFisico la probabilità di colpire fisicamente
     * @param probabilitaColpireMagico la probabilità di colpire magicamente
     * @param possibiliDanniFisici i possibili danni fisici
     * @param possibiliDanniMagici i possibili danni magici
     */
    public InternoRisultatoValutazionePersonaggioAttaccante(Personaggio personaggio, Personaggio bersaglio,
                                                            RisultatoValutazioneAttaccante valutazione, Incantesimo incantesimo,
                                                            double probabilitaColpireFisico, double probabilitaColpireMagico,
                                                            double possibiliDanniFisici, double possibiliDanniMagici) {
        super(TipoEvento.INTERNO_RISULTATO_VALUTAZIONE_PERSONAGGIO_ATTACCANTE, personaggio);
        this.bersaglio = bersaglio;
        this.valutazione = valutazione;
        this.incantesimo = incantesimo;
        this.probabilitaColpireFisico = probabilitaColpireFisico;
        this.probabilitaColpireMagico = probabilitaColpireMagico;
        this.possibiliDanniFisici = possibiliDanniFisici;
        this.possibiliDanniMagici = possibiliDanniMagici;
    }

    /**
     * @return il Personaggio bersaglio della valutazione
     */
    public Personaggio getBersaglio() {
        return bersaglio;
    }

    /**
     * @return il risultato della valutazione
     */
    public RisultatoValutazioneAttaccante getValutazione() {
        return valutazione;
    }

    /**
     * @return l'Incantesimo che viene utilizzato per attaccare
     */
    public Incantesimo getIncantesimo() {
        return incantesimo;
    }

    /**
     * @return la probabilità di colpire fisicamente
     */
    public double getProbabilitaColpireFisico() {
        return probabilitaColpireFisico;
    }

    /**
     * @return la probabilità di colpire magicamente
     */
    public double getProbabilitaColpireMagico() {
        return probabilitaColpireMagico;
    }

    /**
     * @return i possibili danni fisici che il Personaggio attaccante farebbe al Personaggio bersaglio
     */
    public double getPossibiliDanniFisici() {
        return possibiliDanniFisici;
    }

    /**
     * @return i possibili danni magicamente che il Personaggio attaccante farebbe al Personaggio bersaglio
     */
    public double getPossibiliDanniMagici() {
        return possibiliDanniMagici;
    }

    public String getRisultatoValutazione() {
        switch (valutazione) {
            case NON_USA_MAGIA:
            case SENZA_MAGIA_A_DISPOSIZIONE:
            case SENZA_INCANTESIMI_A_DISPOSIZIONE:
            case CASUALMENTE_NON_LANCIA_INCANTESIMO:
                return valutazione.toString();
            case SCEGLIE_A_CASO:
            case SCEGLIE_IL_PIU_POTENTE:
                return valutazione + " " + (incantesimo != null ? incantesimo.getClasse().toString() : "");
            case PREFERISCE_ATTACCO_FISICO:
            case PREFERISCE_ATTACCO_MAGICO:
                return valutazione + " " + formatProbabilita();
            default:
                return "Valutazione non valida";
        }
    }

    private String formatProbabilita() {
        return String.format("%d di colpire FISICO per %d contro %d di colpire MAGICO per %d",
                (int)(probabilitaColpireFisico * 100),
                (int)possibiliDanniFisici,
                (int)(probabilitaColpireMagico * 100),
                (int)possibiliDanniMagici);
    }
}
