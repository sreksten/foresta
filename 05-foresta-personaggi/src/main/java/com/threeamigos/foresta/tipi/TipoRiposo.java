package com.threeamigos.foresta.tipi;

/**
 * Dove il gruppo si riposa: lo dice ogni locazione (Locazione.getTipoRiposo, nel motore). Il moltiplicatore dice quanto
 * vale il riposo rispetto a quello di base: all'aperto senza fuoco recupera la metà, al coperto una volta e mezza. Il
 * recupero di salute, magia e stanchezza lo calcola personaggi.CalcolatoreRiposo; quante ore servono per una notte
 * lo decide il motore (Pernottamento).
 *
 * @author Stefano Reksten
 */
public enum TipoRiposo {

    /**
     * All'aperto, dove non si può accendere un fuoco (la palude).
     */
    ALL_APERTO_SENZA_FUOCO(0.5),
    /**
     * All'aperto, accampati intorno a un fuoco (il bosco, la radura, la grotta, le rovine, il tempio).
     */
    ALL_APERTO_CON_FUOCO(1.0),
    /**
     * In una locanda.
     */
    AL_COPERTO(1.5);

    private final double moltiplicatore;

    TipoRiposo(double moltiplicatore) {
        this.moltiplicatore = moltiplicatore;
    }

    public double getMoltiplicatore() {
        return moltiplicatore;
    }
}
