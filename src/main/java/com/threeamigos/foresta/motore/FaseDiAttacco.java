package com.threeamigos.foresta.motore;

/**
 * Una fase dell'attacco di un turno: con quale arma si colpisce e con che quota del danno
 * (1 per l'arma principale, Costanti.DOPPIA_ARMA_FATTORE_SECONDA_ARMA per la seconda arma).
 */
public class FaseDiAttacco {

    private final Arma arma;
    private final double fattore;

    public FaseDiAttacco(Arma arma, double fattore) {
        this.arma = arma;
        this.fattore = fattore;
    }

    public Arma getArma() {
        return arma;
    }

    public double getFattore() {
        return fattore;
    }
}
