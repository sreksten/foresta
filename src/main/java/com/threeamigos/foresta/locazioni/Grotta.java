package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.Eremita;
import com.threeamigos.foresta.personaggi.Viverna;
import com.threeamigos.foresta.ui.UI;

public class Grotta extends LocazioneBase {

	private static final Grotta istanza = new Grotta();
	
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
		int numero = Dado.tira(2);
		if (numero == 1) {
			int classePersonaggio = Dado.tira(2);
			if (classePersonaggio == 1) {
				gng.aggiungiPersonaggio(new Viverna(Statistiche.getLivello()));
			} else {
				gng.aggiungiPersonaggio(new Eremita(Statistiche.getLivello()));
			}
		}
		if (!isLocazioneVisitata()) {
			setOggetto(new Cofano(Dado.tira(2)));
		}
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		UI.notifica("Qui, in una buia ed umida grotta, " + descrizioneMostriEOggetti(g, gng));
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_CON_FUOCO;
	}
}
