package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.Eremita;
import com.threeamigos.foresta.personaggi.Viverna;
import com.threeamigos.foresta.tools.Random;
import com.threeamigos.foresta.ui.UI;

public class Grotta extends LocazioneBase {

	private static Grotta istanza = new Grotta();
	
	private Grotta() {
	}
	
	public static Grotta getIstanza() {
		return istanza;
	}
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.GROTTA;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		int numero = Random.getInt(2);
		if (numero > 0) {
			int classePersonaggio = Random.getInt(2);
			if (classePersonaggio == 0) {
				gng.aggiungiPersonaggio(new Viverna());
			} else {
				gng.aggiungiPersonaggio(new Eremita());
			}
		}
		if (!isLocazioneVisitata()) {
			setOggetto(new Cofano(Random.getInt(2) + 1));
		}
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		UI.notifica("Qui, in una buia ed umida grotta, " + descrizioneMostriEOggetti(g, gng));
	}
}
