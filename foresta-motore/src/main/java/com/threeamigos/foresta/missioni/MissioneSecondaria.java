package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.tipi.MissionePredefinita;

/**
 * Una missione secondaria generica, senza comportamento: serve da contenitore per le missioni figlie di un'altra (la fabbrica la ricostruisce dopo un caricamento).
 *
 * @author Stefano Reksten
 */
public class MissioneSecondaria extends MissioneBase {

    public MissioneSecondaria() {
        super(MissionePredefinita.MISSIONE_SECONDARIA);
    }

    @Override
    public void controllaPreLocazione() {

    }

    @Override
    public void controllaInLocazione() {

    }

    @Override
    public void controllaPostLocazione() {

    }

    @Override
    public void completaMissione() {
        super.completaMissione();
    }
}
