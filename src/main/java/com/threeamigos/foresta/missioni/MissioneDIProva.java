package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.ui.UI;

/**
 *
 * @author Stefano Reksten
 */
public class MissioneDIProva extends MissioneBase implements Missione {

    public MissioneDIProva() {
        super(ClasseMissione.MISSIONE_DI_PROVA);
        aggiungiMissione(new MissioneDiProvaSecondariaUno());
        aggiungiMissione(new MissioneDiProvaSecondariaDue());
    }

    @Override
    public String getNome() {
        return "Missione di prova";
    }

    @Override
    public String getDescrizione() {
        return "Una missione di prova con missioni secondarie";
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
