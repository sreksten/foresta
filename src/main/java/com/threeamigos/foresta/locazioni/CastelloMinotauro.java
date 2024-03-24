package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.MinotauroGigante;
import com.threeamigos.foresta.ui.UI;

public class CastelloMinotauro extends LocazioneUnica {

	private static CastelloMinotauro istanza = new CastelloMinotauro();
	
	private CastelloMinotauro() {
	}
	
	public static CastelloMinotauro getIstanza() {
		return istanza;
	}
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CASTELLO_MINOTAURO;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		gng.aggiungiPersonaggio(new MinotauroGigante());
		setOggetto(new Cofano(5));
	}

	@Override
	public String getNome() {
		return "la Torre della Paura";
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		UI.notifica(g.chiMaiuscolo() + " arriva alla Torre della Paura. Il Minotauro Gigante, che qui dimora, appare brandendo una spada rossa di sangue!");
	}
	
	@Override
	public void azzeraLocazione(GruppoGiocatore g) {
		if (completa) {
			g.setLocazioneVisitata();
			Foresta.distruggiLocazioneUnica(getClasseLocazione(), ClassiLocazione.ROVINE);
		}
	}
}
