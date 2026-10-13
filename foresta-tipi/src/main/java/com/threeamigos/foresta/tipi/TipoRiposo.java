package com.threeamigos.foresta.tipi;

/**
 * Dove il gruppo si riposa. Il moltiplicatore dice quanto vale il riposo rispetto a quello di base: all'aperto senza
 * fuoco recupera la metà, al coperto una volta e mezza (il calcolo del recupero di salute, magia e stanchezza sta nel
 * motore).
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
