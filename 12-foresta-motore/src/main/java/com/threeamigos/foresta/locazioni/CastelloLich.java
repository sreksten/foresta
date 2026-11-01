package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.Lich;
import com.threeamigos.foresta.tipi.CostantiMotore;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoRiposo;
import com.threeamigos.foresta.testi.Testi;

/**
 * Il Castello dell'Ombra, dove abita il Lich, con dei cofani: se il gruppo lo vince il castello diventa rovine. Non ci si può riposare.
 */
public class CastelloLich extends LocazioneUnica {

	@Override
	public TipoLocazione getTipoLocazione() {
		return TipoLocazione.CASTELLO_LICH;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		gng.aggiungiPersonaggio(new Lich(Statistiche.getLivello()));
		setOggetto(new Cofano(CostantiMotore.COFANI_IN_CASTELLO_LICH));
	}


	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		BusEventi.pubblica(new NotificaTestoParagrafo(Testi.formatta("messaggi_motore", "CastelloLich.1", g.chiMaiuscolo())));
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
		throw new IllegalArgumentException("Non si può riposare nel castello del Lich");
	}
}
