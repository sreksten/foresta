package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.personaggi.Ladra;
import com.threeamigos.foresta.personaggi.Ladro;

public class GrottaRecuperaIlMedaglione extends LocazioneUnica {

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.GROTTA_RECUPERA_IL_MEDAGLIONE;
	}

	@Override
	public String getNome() {
		return "la grotta dei ladri del Medaglione";
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		if (!isCompleta()) {
			int livello = Statistiche.getLivello();
			Ladro ladro = new Ladro(livello);
			ladro.setAmichevole(false);
			ladro.setCorrompibile(false);
			gng.aggiungiPersonaggio(ladro);
			Ladra ladra = new Ladra(livello + 1);
			ladra.setAmichevole(false);
			ladra.setCorrompibile(false);
			gng.aggiungiPersonaggio(ladra);
			// Un livello sotto gli altri, ma almeno il primo
			ladro = new Ladro(Math.max(1, livello - 1));
			ladro.setAmichevole(false);
			ladro.setCorrompibile(false);
			gng.aggiungiPersonaggio(ladro);
			ladro = new Ladro(livello);
			ladro.setAmichevole(false);
			ladro.setCorrompibile(false);
			gng.aggiungiPersonaggio(ladro);
		} else {
			new Grotta().crea(g, gng);
		}
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		if (!isCompleta()) {
			BusEventi.pubblica(new NotificaTestoFrase("Questa è la grotta dove risiede la banda di ladri che ha rubato il Medaglione!"));
		} else {
			BusEventi.pubblica(new NotificaTestoFrase("In questa grotta avevano il loro covo i ladri del medaglione."));
		}
	}
	
	@Override
	public void azzeraLocazione(GruppoGiocatore g) {
		if (isCompleta()) {
			g.setLocazioneCorrenteVisitata();
			// Come per i castelli: la casella diventa una grotta qualsiasi e la locazione unica sparisce dall'elenco
			Foresta.distruggiLocazioneUnica(getClasseLocazione(), ClassiLocazione.GROTTA);
		}
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_CON_FUOCO;
	}

}
