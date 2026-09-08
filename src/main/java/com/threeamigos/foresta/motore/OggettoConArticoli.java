package com.threeamigos.foresta.motore;

/**
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
