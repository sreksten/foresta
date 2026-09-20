package com.threeamigos.foresta.missioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.GruppoGiocatore;

public abstract class Combatti extends MissioneBase {

	protected Combatti(ClasseMissione classe) {
		super(classe);
	}

	@Override
	public String getNome() {
		return "Combatti";
	}

	@Override
	public String getDescrizione() {
		return "Elimina gli avversari";
	}

	@Override
	public void controllaPreLocazione() {
		// Si controlla solamente dopo il completamento della locazione
	}

	@Override
	public void controllaInLocazione() {
		// Si controlla solamente dopo il completamento della locazione
	}

	@Override
	public void controllaPostLocazione() {
		if (GruppoGiocatore.getIstanza().getLocazioneCorrente().isCompleta()) {
			completaMissione();
		}
	}
	
	@Override
	public void completaMissione() {
		super.completaMissione();
		BusEventi.pubblica(new NotificaTestoFrase("La locazione è stata ripulita."));
	}
}
