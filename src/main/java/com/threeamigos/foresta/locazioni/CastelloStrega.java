package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.Strega;
import com.threeamigos.foresta.ui.UI;

public class CastelloStrega extends LocazioneUnica {

	private static CastelloStrega istanza = new CastelloStrega();
	
	private CastelloStrega() {
	}
	
	public static CastelloStrega getIstanza() {
		return istanza;
	}
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CASTELLO_STREGA;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		gng.aggiungiPersonaggio(new Strega());
		setOggetto(new Cofano(5));
	}

	@Override
	public String getNome() {
		return "il Maniero del Malefizio";
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		UI.notifica(g.chiMaiuscolo() + " entra nel Maniero del Malefizio. Ecco arrivare, annunciata da un vento di tempesta, la piu' tremenda alleata del Drago: la Signora della Magia Nera, la Strega!");
	}
	
	@Override
	public void azzeraLocazione(GruppoGiocatore g) {
		if (completa) {
			g.setLocazioneVisitata();
			Foresta.distruggiLocazioneUnica(getClasseLocazione(), ClassiLocazione.ROVINE);
		}
	}
}
