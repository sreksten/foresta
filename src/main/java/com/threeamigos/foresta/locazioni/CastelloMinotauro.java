package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.EventoParagrafo;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.MinotauroGigante;

public class CastelloMinotauro extends LocazioneUnica {

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CASTELLO_MINOTAURO;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		gng.aggiungiPersonaggio(new MinotauroGigante(Statistiche.getLivello()));
		setOggetto(new Cofano(Costanti.COFANI_IN_CASTELLO_MINOTAURO));
	}

	@Override
	public String getNome() {
		return "la Torre della Paura";
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		BusEventi.pubblica(new EventoParagrafo(g.chiMaiuscolo() + " arriva alla Torre della Paura. Il Minotauro Gigante, che qui dimora, appare brandendo una spada rossa di sangue!"));
	}
	
	@Override
	public void azzeraLocazione(GruppoGiocatore g) {
		if (isCompleta()) {
			g.setLocazioneCorrenteVisitata();
			Foresta.distruggiLocazioneUnica(getClasseLocazione(), ClassiLocazione.ROVINE);
		}
	}

	public TipoRiposo getTipoRiposo() {
		throw new IllegalArgumentException("Non si può riposare nel castello del Minotauro Gigante");
	}
}
