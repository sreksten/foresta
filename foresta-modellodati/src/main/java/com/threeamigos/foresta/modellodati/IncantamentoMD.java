package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoDanno;

/**
 * Un incantamento di un artefatto: un danno elementale che si aggiunge a quello dell'arma. Il danno è composto da una
 * parte fissa e da una che cresce con l'attributo del portatore (vedi {@link #getCoefficienteScala()}).
 * <p>
 * È immutabile.
 *
 * @author Stefano Reksten
 */
public class IncantamentoMD {

    private final String nomeIncantamento;
    private final TipoDanno tipoDannoElementale; // es. FUOCO
    private final int dannoBonusFisso;           // es. 4 danni bonus
    private final double coefficienteScala;       // es. 0.2 (scala su Intelligenza)

    /**
     * @param nomeIncantamento il nome dell'incantamento; il carattere "|" si toglie e un nome vuoto diventa {@link ArtefattoMD#NESSUN_NOME}
     * @param tipoDannoElementale il tipo di danno elementale, per esempio il fuoco
     * @param dannoBonusFisso i danni in più che l'incantamento aggiunge sempre
     * @param coefficienteScala la quota dell'attributo del portatore che si aggiunge ai danni (0,2 è il 20%)
     */
    public IncantamentoMD(String nomeIncantamento, TipoDanno tipoDannoElementale, int dannoBonusFisso, double coefficienteScala) {
        String pulito = Serializzabile.senzaPipe(nomeIncantamento);
        this.nomeIncantamento = pulito == null || pulito.trim().isEmpty() ? ArtefattoMD.NESSUN_NOME : pulito;
        this.tipoDannoElementale = tipoDannoElementale;
        this.dannoBonusFisso = dannoBonusFisso;
        this.coefficienteScala = coefficienteScala;
    }

    /**
     * Il nome dell'incantamento.
     */
    public String getNomeIncantamento() {
        return nomeIncantamento;
    }

    /**
     * Il tipo di danno elementale che l'incantamento aggiunge.
     */
    public TipoDanno getTipoDannoElementale() {
        return tipoDannoElementale;
    }

    /**
     * I danni in più, sempre uguali.
     */
    public int getDannoBonusFisso() {
        return dannoBonusFisso;
    }

    /**
     * La quota dell'attributo del portatore (l'intelligenza, per esempio) che si aggiunge ai danni.
     */
    public double getCoefficienteScala() {
        return coefficienteScala;
    }
}
