package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;

/**
 * Missione di prova, per controllare nella finestra delle missioni come appare una missione fallita: si attiva al
 * primo turno e fallisce al secondo.
 */
public class MissioneCheFallisce extends MissioneBase implements Missione {

    public MissioneCheFallisce() {
        super(ClasseMissione.MISSIONE_CHE_FALLISCE);
    }

    @Override
    public String getNome() {
        return "Missione che fallisce";
    }

    @Override
    public String getDescrizione() {
        return "Una missione di prova che fallisce al secondo turno";
    }

    @Override
    public void controllaPreLocazione() {
        if (!isAttiva()) {
            BusEventi.pubblica(new NotificaTestoParagrafo(getDescrizione()));
            attivaMissione();
        } else if (!isFallita()) {
            BusEventi.pubblica(new NotificaTestoParagrafo("La missione di prova è fallita, come previsto"));
            fallisciMissione();
        }
    }

    @Override
    public void controllaInLocazione() {
        // Non succede nulla
    }

    @Override
    public void controllaPostLocazione() {
        // Non succede nulla
    }
}
