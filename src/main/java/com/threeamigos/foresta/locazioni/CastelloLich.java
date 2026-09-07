package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.Lich;
import com.threeamigos.foresta.ui.UI;

public class CastelloLich extends LocazioneUnica {

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CASTELLO_LICH;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		gng.aggiungiPersonaggio(new Lich(Statistiche.getLivello()));
		setOggetto(new Cofano(Costanti.COFANI_IN_CASTELLO_LICH));
	}

	@Override
	public String getNome() {
		return "il Castello dell'Ombra";
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		UI.notifica(g.chiMaiuscolo() + " giunge al Castello dell'Ombra. Da una nuvola di fumo nero ecco apparire il più spaventoso tra i non morti: il Lich!");
	}
	
	@Override
	public void azzeraLocazione(GruppoGiocatore g) {
		if (isCompleta()) {
			g.setLocazioneCorrenteVisitata();
			Foresta.distruggiLocazioneUnica(getClasseLocazione(), ClassiLocazione.ROVINE);
		}
	}

	public TipoRiposo getTipoRiposo() {
		throw new IllegalArgumentException("Non si può riposare nel castello del Lich");
	}
}
