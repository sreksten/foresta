package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoParagrafo;

/**
 *
 * @author Stefano Reksten
 */
public class MissioneDiProvaSecondariaUno extends MissioneBase implements Missione {

    public MissioneDiProvaSecondariaUno() {
        super(ClasseMissione.MISSIONE_DI_PROVA_SECONDARIA_UNO);
        aggiungiMissione(new MissioneDiProvaTerziariaUno());
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
            BusEventi.pubblica(new EventoParagrafo(getDescrizione()));
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
