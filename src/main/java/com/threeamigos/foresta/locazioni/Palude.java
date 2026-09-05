package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.ui.UI;

public class Palude extends LocazioneBase {

	private static final Palude istanza = new Palude();
	
	private Palude() {
	}
	
	public static Palude getIstanza() {
		return istanza;
	}
	
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
		UI.notifica("Qui, nel mezzo di una malsana e pericolosa palude, " + g.chi() + " non trova nulla.");
	}

	@Override
	public Stato impostaAzioni(GruppoGiocatore g, GruppoAvversario gng, Comando azione) {
		if (isLocazioneVisitata() || Dado.tira(10) > 3) {
			UI.notifica("Il posto però non promette nulla di buono e " + g.chi() + " decide di non restare.");
			return Stato.FINE_LOCAZIONE;
		}
		UI.notifica("Approfittando della quiete del posto, " + g.chi() + " riposerà un poco.");
		g.riposa(getTipoRiposo());
		return Stato.FINE_LOCAZIONE;
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_SENZA_FUOCO;
	}
}
