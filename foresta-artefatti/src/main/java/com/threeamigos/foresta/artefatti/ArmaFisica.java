package com.threeamigos.foresta.artefatti;


import com.threeamigos.foresta.danni.tipi.TipoDanno;
import com.threeamigos.foresta.modificatori.ModificatoreAttributoMD;
import com.threeamigos.foresta.modificatori.tipi.BersaglioModificatore;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Un artefatto che è un'arma: i danni, il tipo di danno, gli incantamenti e i modificatori che mette sul bersaglio vengono dal suo
 * {@code ArtefattoMD}.
 *
 * @author Stefano Reksten
 */
public class ArmaFisica extends Artefatto implements Arma {

    /**
     * Un'arma sui dati dati.
     */
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

    @Override
    public Collection<ModificatoreAttributoMD> getModificatoriSulBersaglio() {
        return md.getModificatori().stream()
                .filter(m -> m.getBersaglio() == BersaglioModificatore.BERSAGLIO)
                .collect(Collectors.toList());
    }
}
