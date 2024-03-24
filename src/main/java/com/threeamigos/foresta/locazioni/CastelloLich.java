package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.Lich;
import com.threeamigos.foresta.ui.UI;

public class CastelloLich extends LocazioneUnica {

	private static CastelloLich istanza = new CastelloLich();
	
	private CastelloLich() {
	}
	
	public static CastelloLich getIstanza() {
		return istanza;
	}
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CASTELLO_LICH;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		gng.aggiungiPersonaggio(new Lich());
		setOggetto(new Cofano(5));
	}

	@Override
	public String getNome() {
		return "il Castello dell'Ombra";
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		UI.notifica(g.chiMaiuscolo() + " giunge al Castello dell'Ombra. Da una nuvola di fumo nero ecco apparire il piu' tremendo tra i non morti: il Lich!");
	}
	
	@Override
	public void azzeraLocazione(GruppoGiocatore g) {
		if (completa) {
			g.setLocazioneVisitata();
			Foresta.distruggiLocazioneUnica(getClasseLocazione(), ClassiLocazione.ROVINE);
		}
	}
}
