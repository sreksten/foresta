package com.threeamigos.foresta.interfacce;

/**
 * Un oggetto di cui si possono scrivere i nomi con l'articolo giusto (il, un, alcuni...), per costruire le frasi dei testi.
 *
 * @author Stefano Reksten
 */
public interface OggettoConArticoli {

    /**
     * Articolo indeterminativo singolare
     */
    String getAIS();
    /**
     * Una sorta di "articolo indeterminativo plurale" (alcuni, alcune)
     */
    String getAIP();
    /**
     * Articolo determinativo singolare
     */
    String getADS();
    /**
     * Articolo determinativo plurale
     */
    String getADP();

}
