package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.personaggi.Drago;

public class CastelloDrago extends LocazioneUnica {

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CASTELLO_DRAGO;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		gng.aggiungiPersonaggio(new Drago(Statistiche.getLivello()));
	}

	@Override
	public String getNome() {
		return "il Castello della Morte Alata";
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		BusEventi.pubblica(new NotificaTestoParagrafo(g.chi() + " entra al Castello della Morte Alata, per lo scontro finale. Con uno sbatter d'ali simile al rombo di tuono, la Grande Minaccia si erge ora davanti al gruppo: il Drago è qui!"));
	}

	@Override
	public void azzeraLocazione(GruppoGiocatore g) {
		if (isCompleta()) {
			LineaTemporale.setDragoSconfitto(true);
			g.setLocazioneCorrenteVisitata();
			Foresta.distruggiLocazioneUnica(getClasseLocazione(), ClassiLocazione.ROVINE);
		}
	}

	public TipoRiposo getTipoRiposo() {
		throw new IllegalArgumentException("Non si può riposare nel castello del Drago");
	}
}
