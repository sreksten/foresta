package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoMessaggio;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.personaggi.Troll;

public class RovineRecuperaLeDerrateAlimentari extends LocazioneUnica {

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
			new Rovine().crea(g, gng);
		}
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		if (!isCompleta()) {
			BusEventi.pubblica(new EventoMessaggio("Tra queste rovine si nasconde la banda di Troll che ha rubato il carico di derrate alimentari!"));
		} else {
			BusEventi.pubblica(new EventoMessaggio("Tra queste rovine si nascondeva la banda di Troll che aveva rubato il carico di derrate alimentari."));
		}
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_CON_FUOCO;
	}

}
