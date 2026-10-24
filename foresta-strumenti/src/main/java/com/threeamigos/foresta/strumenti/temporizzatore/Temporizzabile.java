package com.threeamigos.foresta.strumenti.temporizzatore;

/**
 * Un oggetto che può rispondere a un "tick" di un temporizzatore
 *
 * @author Stefano Reksten
 */
public interface Temporizzabile {

    /**
     * Un impulso del temporizzatore.
     */
    void tick();

}
