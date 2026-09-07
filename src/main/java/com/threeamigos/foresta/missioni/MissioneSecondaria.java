package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.motore.modellodati.MissioneMD;

/**
 *
 * @author Stefano Reksten
 */
public class MissioneSecondaria extends MissioneBase {

    public MissioneSecondaria(MissioneMD missioneMD) {
        setModelloDati(md);
        for (MissioneMD missioneSecondariaMD : missioneMD.getMissioniMD()) {
            aggiungiMissione(new MissioneSecondaria(missioneSecondariaMD));
        }
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
