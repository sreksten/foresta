package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;

/**
 *
 * @author Stefano Reksten
 */
public class MissioneDIProva extends MissioneBase implements Missione {

    public MissioneDIProva() {
        super(ClasseMissione.MISSIONE_DI_PROVA);
//        aggiungiMissione(new MissioneDiProvaSecondariaUno());
//        aggiungiMissione(new MissioneDiProvaSecondariaDue());
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
            BusEventi.pubblica(new NotificaTestoParagrafo(getDescrizione()));
            attivaMissione();
        }
    }

    @Override
    public void controllaInLocazione() {

    }

    @Override
    public void controllaPostLocazione() {
        if (!isCompleta()) {
            BusEventi.pubblica(new NotificaTestoParagrafo("Missione di prova completata"));
            completaMissione();
        }
    }
}
