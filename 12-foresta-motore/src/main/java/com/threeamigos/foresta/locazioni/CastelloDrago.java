package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.statistiche.LineaTemporale;
import com.threeamigos.foresta.mappa.Foresta;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.personaggi.Drago;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoRiposo;

/**
 * Il Castello della Morte Alata, dove abita il Drago, per lo scontro finale: se il gruppo lo vince, il drago è sconfitto e il castello
 * diventa rovine. Non ci si può riposare.
 */
public class CastelloDrago extends LocazioneUnica {

	@Override
	public TipoLocazione getTipoLocazione() {
		return TipoLocazione.CASTELLO_DRAGO;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		gng.aggiungiPersonaggio(new Drago(Statistiche.getLivello()));
	}


	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		BusEventi.pubblica(new NotificaTestoParagrafo(g.chi() + " entra al Castello della Morte Alata, per lo scontro finale. Con uno sbatter d'ali simile al rombo di tuono, la Grande Minaccia si erge ora davanti al gruppo: il Drago è qui!"));
	}

	@Override
	public void azzeraLocazione(GruppoGiocatore g) {
		// La base libera la locazione dagli avversari rimasti, la segna visitata se completa e toglie la furia al gruppo
		super.azzeraLocazione(g);
		if (isCompleta()) {
			LineaTemporale.setDragoSconfitto(true);
			Foresta.distruggiLocazioneUnica(getTipoLocazione(), TipoLocazione.ROVINE);
		}
	}

	public TipoRiposo getTipoRiposo() {
		throw new IllegalArgumentException("Non si può riposare nel castello del Drago");
	}
}
