package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.motore.modellodati.TipoDanno;
import com.threeamigos.foresta.oggetti.Incantamento;

import java.util.Collection;

/**
 *
 * @author Stefano Reksten
 */
public interface Arma {

    int getDanni();
    int getLivello();
    TipoDanno getTipoDanno();

    boolean isIncantata();
    Collection<Incantamento> getIncantamenti();

}
