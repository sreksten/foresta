package com.threeamigos.foresta.artefatti;

import com.threeamigos.foresta.modellodati.IncantamentoMD;
import com.threeamigos.foresta.tipi.TipoDanno;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;

import java.util.Collection;
import java.util.Collections;

/**
 * Un'arma, vista da chi non ne conosce la classe concreta: i danni base, il livello, il tipo di danno e gli eventuali incantamenti.
 *
 * @author Stefano Reksten
 */
public interface Arma {

    /**
     * I danni base dell'arma, prima di incantamenti e attributi.
     */
    int getDanni();

    /**
     * Il livello dell'arma.
     */
    int getLivello();

    /**
     * Il tipo di danno fisico che l'arma dà.
     */
    TipoDanno getTipoDanno();

    /**
     * Se l'arma ha almeno un incantamento.
     */
    boolean isIncantata();

    /**
     * Se è un attacco magico (un incantesimo, il dardo arcano, un'abilità di danno) e non un'arma: i danni scalano con l'INTELLIGENZA e con
     * il potere magico di chi lo usa. Un'arma, anche incantata, non lo è.
     */
    default boolean isMagica() {
        return false;
    }

    /**
     * Gli incantamenti dell'arma.
     */
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
