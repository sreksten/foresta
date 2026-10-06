package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.modellodati.IncantamentoMD;

import com.threeamigos.foresta.interfacce.Arma;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.tipi.TipoDanno;

import java.util.Collection;

/**
 *
 * @author Stefano Reksten
 */
public class ArmaFisica extends Artefatto implements Arma {

    public ArmaFisica(ArtefattoMD md) {
        super(md);
    }

    public int getDanni() {
        return md.getDanni();
    }

    @Override
    public TipoDanno getTipoDanno() {
        return md.getTipo().getTipoDanno();
    }

    @Override
    public boolean isIncantata() {
        return !md.getIncantamenti().isEmpty();
    }

    @Override
    public Collection<IncantamentoMD> getIncantamenti() {
        return md.getIncantamenti();
    }
}
