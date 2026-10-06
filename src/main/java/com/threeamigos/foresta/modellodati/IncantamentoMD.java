package com.threeamigos.foresta.modellodati;

import com.threeamigos.foresta.tipi.TipoDanno;

/**
 *
 * @author Stefano Reksten
 */
public class IncantamentoMD {

    private final String nomeIncantamento;
    private final TipoDanno tipoDannoElementale; // es. FUOCO
    private final int dannoBonusFisso;           // es. 4 danni bonus
    private final double coefficienteScala;       // es. 0.2 (scala su Intelligenza)

    public IncantamentoMD(String nomeIncantamento, TipoDanno tipoDannoElementale, int dannoBonusFisso, double coefficienteScala) {
        String pulito = Serializzabile.senzaPipe(nomeIncantamento);
        this.nomeIncantamento = pulito == null || pulito.trim().isEmpty() ? ArtefattoMD.NESSUN_NOME : pulito;
        this.tipoDannoElementale = tipoDannoElementale;
        this.dannoBonusFisso = dannoBonusFisso;
        this.coefficienteScala = coefficienteScala;
    }

    public String getNomeIncantamento() {
        return nomeIncantamento;
    }

    public TipoDanno getTipoDannoElementale() {
        return tipoDannoElementale;
    }

    public int getDannoBonusFisso() {
        return dannoBonusFisso;
    }

    public double getCoefficienteScala() {
        return coefficienteScala;
    }
}
