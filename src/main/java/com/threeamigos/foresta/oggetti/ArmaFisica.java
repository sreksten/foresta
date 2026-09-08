package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Arma;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.TipoDanno;

/**
 *
 * @author Stefano Reksten
 */
public class ArmaFisica extends Artefatto implements Arma {

    public ArmaFisica(ArtefattoMD md) {
        super(md);
    }

    @Override
    public TipoDanno getTipoDanno() {
        return null;
    }
}
