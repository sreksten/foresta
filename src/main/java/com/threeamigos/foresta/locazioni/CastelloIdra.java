package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.Idra;
import com.threeamigos.foresta.ui.UI;

public class CastelloIdra extends LocazioneUnica {

	private static CastelloIdra istanza = new CastelloIdra();
	
	private CastelloIdra() {
	}
	
	public static CastelloIdra getIstanza() {
		return istanza;
	}
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CASTELLO_IDRA;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		gng.aggiungiPersonaggio(new Idra());
		setOggetto(new Cofano(5));
	}

	@Override
	public String getNome() {
		return "la Rocca del Sangue";
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		UI.notifica(g.chiMaiuscolo() + " arriva alla Rocca del Sangue. L'Idra a tre teste si para davanti sibilando minacciosa!");
	}
	
	@Override
	public void azzeraLocazione(GruppoGiocatore g) {
		if (completa) {
			g.setLocazioneVisitata();
			Foresta.distruggiLocazioneUnica(getClasseLocazione(), ClassiLocazione.ROVINE);
		}
	}
}
