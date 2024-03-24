package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.ClassiPersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tools.Random;
import com.threeamigos.foresta.ui.UI;

public class Tempio extends LocazioneBase {

	private static Tempio istanza = new Tempio();
	
	private Tempio() {
	}
	
	public static Tempio getIstanza() {
		return istanza;
	}
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.TEMPIO;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		int nViverne;
		int nCofani;
		Artefatto a = getArtefatto(g);
		if (a == null) {
			nViverne = Random.getInt(5);
			if (!isLocazioneVisitata()) {
				nCofani = Random.getInt(6);
				setOggetto(new Cofano(nCofani));
			}
		} else {
			nViverne = 4 + Random.getInt(4);
			setOggetto(a);
		}
		Personaggio p;
		for (int i = 0; i < nViverne; i++) {
			p = ClassiPersonaggio.VIVERNA.getIstanza();
			p.setOrdinale(i + 1);
			gng.aggiungiPersonaggio(p);
		}
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		UI.notifica("Qui, in un tempio, " + descrizioneMostriEOggetti(g, gng));
	}
}
