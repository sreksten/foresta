package com.threeamigos.foresta.motore.modellodati;

/**
 *
 * @author Stefano Reksten
 */
public enum TipoRiposo {

    ALL_APERTO_SENZA_FUOCO(0.5),
    ALL_APERTO_CON_FUOCO(1.0),
    AL_COPERTO(1.5);

    private final double moltiplicatore;

    TipoRiposo(double moltiplicatore) {
        this.moltiplicatore = moltiplicatore;
    }

    public double getMoltiplicatore() {
        return moltiplicatore;
    }
}
