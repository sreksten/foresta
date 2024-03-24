package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Stato;
import com.threeamigos.foresta.tools.Random;
import com.threeamigos.foresta.ui.UI;

public class Palude extends LocazioneBase {

	private static Palude istanza = new Palude();
	
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
		// niente da fare
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		UI.notifica("Qui, nel mezzo di una malsana e pericolosa palude, " + g.chi() + " non trova nulla.");
	}

	@Override
	public Stato impostaAzioni(GruppoGiocatore g, GruppoAvversario gng, Comando azione) {
		if (Random.getInt(10) > 3) {
			UI.notifica("Il posto pero' non promette nulla di buono e " + g.chi() + " decide di non restare.");
			return Stato.FINE_LOCAZIONE;
		}
		UI.notifica("Approfittando della quiete del posto, " + g.chi() + " riposera' un poco.");
		g.riposa();
		return Stato.FINE_LOCAZIONE;
	}
}
