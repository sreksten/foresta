package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.oggetti.Incantamento;
import com.threeamigos.foresta.tipi.TipoDanno;

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
