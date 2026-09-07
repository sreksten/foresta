package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.ui.UI;

/**
 *
 * @author Stefano Reksten
 */
public class MissioneDiProvaSecondariaUno extends MissioneBase implements Missione {

    public MissioneDiProvaSecondariaUno() {
        missioniSecondarie.add(new MissioneDiProvaTerziariaUno());
    }

    @Override
    public String getNome() {
        return "Prova secondaria uno";
    }

    @Override
    public String getDescrizione() {
        return "Missione secondaria di una altra missione";
    }

    @Override
    public void controllaPreLocazione() {
        if (!isAttiva()) {
            UI.notifica(getDescrizione());
            attivaMissione();
        }
    }

    @Override
    public void controllaInLocazione() {

    }

    @Override
    public void controllaPostLocazione() {

    }
}
