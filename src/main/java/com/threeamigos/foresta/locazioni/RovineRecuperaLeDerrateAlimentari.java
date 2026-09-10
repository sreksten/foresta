package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.personaggi.Troll;
import com.threeamigos.foresta.ui.UI;

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
			UI.notifica("Tra queste rovine si nasconde la banda di Troll che ha rubato il carico di derrate alimentari!");
		} else {
			UI.notifica("Tra queste rovine si nascondeva la banda di Troll che aveva rubato il carico di derrate alimentari.");
		}
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_CON_FUOCO;
	}

}
