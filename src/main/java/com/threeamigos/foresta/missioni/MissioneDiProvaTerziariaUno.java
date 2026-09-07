package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.ui.UI;

/**
 *
 * @author Stefano Reksten
 */
public class MissioneDiProvaTerziariaUno extends MissioneBase implements Missione {

    @Override
    public String getNome() {
        return "Prova terziaria uno";
    }

    @Override
    public String getDescrizione() {
        return "Missione terziaria di una altra missione";
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
