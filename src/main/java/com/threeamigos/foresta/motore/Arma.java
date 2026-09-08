package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.TipoDanno;

/**
 *
 * @author Stefano Reksten
 */
public interface Arma {

    int getDanni();
    int getLivello();
    TipoDanno getTipoDanno();

}
