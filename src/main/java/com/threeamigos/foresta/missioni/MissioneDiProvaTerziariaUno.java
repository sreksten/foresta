package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoParagrafo;

/**
 *
 * @author Stefano Reksten
 */
public class MissioneDiProvaTerziariaUno extends MissioneBase implements Missione {

	public MissioneDiProvaTerziariaUno() {
		super(ClasseMissione.MISSIONE_DI_PROVA_TERZIARIA_UNO);
	}

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
