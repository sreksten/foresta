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
     * Se l'arma "dà" quel tipo di danno: è il suo tipo di danno, o quello di uno dei suoi incantamenti (una spada
     * tagliente con un incantamento SACRO dà TAGLIENTE e SACRO). Non c'entrano i modificatori, che cambiano gli attributi.
     */
    default boolean daTipoDanno(TipoDanno tipoDanno) {
        if (getTipoDanno() == tipoDanno) {
            return true;
        }
        for (IncantamentoMD incantamento : getIncantamenti()) {
            if (incantamento.getTipoDannoElementale() == tipoDanno) {
                return true;
            }
        }
        return false;
    }

    /**
     * I modificatori che l'arma, colpendo, può mettere sul bersaglio (quelli scritti con bersaglio BERSAGLIO)
     */
    default Collection<ModificatoreAttributoMD> getModificatoriSulBersaglio() {
        return Collections.emptyList();
    }

}
