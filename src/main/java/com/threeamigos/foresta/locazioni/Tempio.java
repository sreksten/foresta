package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;

public class Tempio extends LocazioneBase {

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.TEMPIO;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		int nViverne;
        Artefatto a = getArtefatto(g);
		if (a == null) {
			nViverne = Dado.tira(5);
			if (!isLocazioneVisitata()) {
				setOggetto(new Cofano(Dado.tira(0, 5)));
			}
		} else {
			nViverne = Dado.tira(4, 7);
			setOggetto(a);
		}
		Personaggio p;
		for (int i = 0; i < nViverne; i++) {
			p = ClassePersonaggio.VIVERNA.getIstanza(Statistiche.getLivello());
			p.setOrdinale(i + 1);
			gng.aggiungiPersonaggio(p);
		}
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		BusEventi.pubblica(new NotificaTestoFrase("Qui, in un tempio, " + descrizioneMostriEOggetti(g, gng)));
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_CON_FUOCO;
	}
}
