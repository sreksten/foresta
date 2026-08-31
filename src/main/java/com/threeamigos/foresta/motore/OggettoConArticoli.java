package com.threeamigos.foresta.motore;

/**
 *
 * @author Stefano Reksten
 */
public interface OggettoConArticoli {

    /**
     * Articolo indeterminativo singolare
     */
    public String getAIS();
    /**
     * Una sorta di "articolo indeterminativo plurale" (alcuni, alcune)
     */
    public String getAIP();
    /**
     * Articolo determinativo singolare
     */
    public String getADS();
    /**
     * Articolo determinativo plurale
     */
    public String getADP();

}
