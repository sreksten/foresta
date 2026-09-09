package com.threeamigos.foresta.eventi;

import com.threeamigos.foresta.incantesimi.Incantesimo;
import com.threeamigos.foresta.motore.modellodati.RisultatoValutazioneAttaccante;
import com.threeamigos.foresta.personaggi.Personaggio;

/**
 *
 * @author Stefano Reksten
 */
public class EventoValutazioneAttaccante extends EventoPersonaggio{

    private final Personaggio bersaglio;
    private final RisultatoValutazioneAttaccante valutazione;
    private final Incantesimo incantesimo;
    private final double probabilitaColpireFisico;
    private final double probabilitaColpireMagico;
    private final double possibiliDanniFisici;
    private final double possibiliDanniMagici;

    public EventoValutazioneAttaccante(Personaggio personaggio, Personaggio bersaglio,
                                       RisultatoValutazioneAttaccante valutazione) {
        super(TipoEvento.PERSONAGGIO_VALUTAZIONE, personaggio);
        this.bersaglio = bersaglio;
        this.valutazione = valutazione;
        this.incantesimo = null;
        probabilitaColpireFisico = -1;
        probabilitaColpireMagico = -1;
        possibiliDanniFisici = -1;
        possibiliDanniMagici = -1;
    }

    public EventoValutazioneAttaccante(Personaggio personaggio, Personaggio bersaglio,
                                       RisultatoValutazioneAttaccante valutazione, Incantesimo incantesimo) {
        super(TipoEvento.PERSONAGGIO_VALUTAZIONE, personaggio);
        this.bersaglio = bersaglio;
        this.valutazione = valutazione;
        this.incantesimo = incantesimo;
        probabilitaColpireFisico = -1;
        probabilitaColpireMagico = -1;
        possibiliDanniFisici = -1;
        possibiliDanniMagici = -1;
    }

    public EventoValutazioneAttaccante(Personaggio personaggio, Personaggio bersaglio,
                                       RisultatoValutazioneAttaccante valutazione, Incantesimo incantesimo,
                                       double probabilitaColpireFisico, double probabilitaColpireMagico,
                                       double possibiliDanniFisici, double possibiliDanniMagici) {
        super(TipoEvento.PERSONAGGIO_VALUTAZIONE, personaggio);
        this.bersaglio = bersaglio;
        this.valutazione = valutazione;
        this.incantesimo = incantesimo;
        this.probabilitaColpireFisico = probabilitaColpireFisico;
        this.probabilitaColpireMagico = probabilitaColpireMagico;
        this.possibiliDanniFisici = possibiliDanniFisici;
        this.possibiliDanniMagici = possibiliDanniMagici;
    }

    public Personaggio getBersaglio() {
        return bersaglio;
    }

    public RisultatoValutazioneAttaccante getValutazione() {
        return valutazione;
    }

    public Incantesimo getIncantesimo() {
        return incantesimo;
    }

    public double getProbabilitaColpireFisico() {
        return probabilitaColpireFisico;
    }

    public double getProbabilitaColpireMagico() {
        return probabilitaColpireMagico;
    }

    public double getPossibiliDanniFisici() {
        return possibiliDanniFisici;
    }

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
