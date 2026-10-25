package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.MinotauroGigante;
import com.threeamigos.foresta.tipi.CostantiMotore;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoRiposo;

/**
 * La Torre della Paura, dove abita il Minotauro Gigante, con dei cofani: se il gruppo lo vince il castello diventa rovine. Non ci si può riposare.
 */
public class CastelloMinotauro extends LocazioneUnica {

	@Override
	public TipoLocazione getTipoLocazione() {
		return TipoLocazione.CASTELLO_MINOTAURO;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		gng.aggiungiPersonaggio(new MinotauroGigante(Statistiche.getLivello()));
		setOggetto(new Cofano(CostantiMotore.COFANI_IN_CASTELLO_MINOTAURO));
	}


	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		BusEventi.pubblica(new NotificaTestoParagrafo(g.chiMaiuscolo() + " arriva alla Torre della Paura. Il Minotauro Gigante, che qui dimora, appare brandendo una spada rossa di sangue!"));
	}
	
	@Override
	public void azzeraLocazione(GruppoGiocatore g) {
		// La base libera la locazione dagli avversari rimasti, la segna visitata se completa e toglie la furia al gruppo
		super.azzeraLocazione(g);
		if (isCompleta()) {
			Foresta.distruggiLocazioneUnica(getTipoLocazione(), TipoLocazione.ROVINE);
		}
	}

	public TipoRiposo getTipoRiposo() {
		throw new IllegalArgumentException("Non si può riposare nel castello del Minotauro Gigante");
	}
}
