package com.threeamigos.foresta.interfacce;

import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.tipi.TipoDanno;

import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author Stefano Reksten
 */
public interface Arma {

    int getDanni();
    int getLivello();
    TipoDanno getTipoDanno();

    boolean isIncantata();
    Collection<IncantamentoMD> getIncantamenti();

    /**
     * I modificatori che l'arma, colpendo, può mettere sul bersaglio (quelli scritti con bersaglio BERSAGLIO)
     */
    default Collection<ModificatoreAttributoMD> getModificatoriSulBersaglio() {
        return Collections.emptyList();
    }

}
