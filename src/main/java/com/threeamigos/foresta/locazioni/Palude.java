package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;

public class Palude extends LocazioneBase {

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.PALUDE;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		// niente da creare
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		BusEventi.pubblica(new NotificaTestoFrase("Qui, nel mezzo di una malsana e pericolosa palude, " + g.chi() + " non trova nulla."));
	}

	@Override
	public Stato impostaAzioni(GruppoGiocatore g, GruppoAvversario gng, Comando azione) {
		if (isLocazioneVisitata() || Dado.tira(10) > 3) {
			BusEventi.pubblica(new NotificaTestoFrase("Il posto però non promette nulla di buono e " + g.chi() + " decide di non restare."));
			setCompleta(true);
			return Stato.FINE_LOCAZIONE;
		}
		BusEventi.pubblica(new NotificaTestoFrase("Approfittando della quiete del posto, " + g.chi() + " riposerà un poco."));
		g.riposa(getTipoRiposo());
		setCompleta(true);
		return Stato.FINE_LOCAZIONE;
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_SENZA_FUOCO;
	}
}
