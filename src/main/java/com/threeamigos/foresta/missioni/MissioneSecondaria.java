package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.tipi.ClasseMissione;

/**
 *
 * @author Stefano Reksten
 */
public class MissioneSecondaria extends MissioneBase {

    public MissioneSecondaria() {
        super(ClasseMissione.MISSIONE_SECONDARIA);
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
