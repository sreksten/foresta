package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoParagrafo;

/**
 *
 * @author Stefano Reksten
 */
public class MissioneDiProvaSecondariaDue extends MissioneBase implements Missione {

	public MissioneDiProvaSecondariaDue() {
		super(ClasseMissione.MISSIONE_DI_PROVA_SECONDARIA_DUE);
	}

    @Override
    public String getNome() {
        return "Prova secondaria due";
    }

    @Override
    public String getDescrizione() {
        return "Missione secondaria due di una altra missione";
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
