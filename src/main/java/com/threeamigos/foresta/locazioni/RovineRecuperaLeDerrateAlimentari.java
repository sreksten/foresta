package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Troll;

public class RovineRecuperaLeDerrateAlimentari extends LocazioneUnica {

	// Recuperate le derrate, sono rovine come le altre: mostri e oggetti vengono da qui
	private final Rovine rovine = new Rovine();

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI;
	}

	@Override
	public String getNome() {
		return "il covo dei Troll che hanno rubato il carico di derrate alimentari";
	}	

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		if (!isCompleta()) {
			for (int i = 0; i < 7; i++) {
				Troll troll = new Troll(Statistiche.getLivello());
				troll.setCorrompibile(false);
				gng.aggiungiPersonaggio(troll);
			}
		} else {
			// Mostri e oggetto di rovine qualsiasi, ma su questa locazione: su un'istanza di Rovine usa e getta
			// l'oggetto andrebbe perso
			super.crea(g, gng);
		}
	}

	@Override
	protected ClassePersonaggio[] getPossibiliIncontri() {
		return rovine.getPossibiliIncontri();
	}

	@Override
	protected ClassiOggetto[] getPossibiliOggetti() {
		return rovine.getPossibiliOggetti();
	}

	/**
	 * Recuperate le derrate, la casella torna a essere delle rovine qualsiasi (come fa la grotta del medaglione).
	 */
	@Override
	public void azzeraLocazione(GruppoGiocatore g) {
		if (isCompleta()) {
			g.setLocazioneCorrenteVisitata();
			Foresta.distruggiLocazioneUnica(getClasseLocazione(), ClassiLocazione.ROVINE);
		}
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		if (!isCompleta()) {
			BusEventi.pubblica(new NotificaTestoFrase("Tra queste rovine si nasconde la banda di Troll che ha rubato il carico di derrate alimentari!"));
		} else {
			BusEventi.pubblica(new NotificaTestoFrase("Tra queste rovine si nascondeva la banda di Troll che aveva rubato il carico di derrate alimentari. "
					+ descrizioneMostriEOggetti(g, gng)));
		}
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_CON_FUOCO;
	}

}
