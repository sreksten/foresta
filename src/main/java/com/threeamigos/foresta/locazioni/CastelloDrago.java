package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.LineaTemporale;
import com.threeamigos.foresta.personaggi.Drago;
import com.threeamigos.foresta.ui.UI;

public class CastelloDrago extends LocazioneUnica {

	private static CastelloDrago istanza = new CastelloDrago();
	
	private CastelloDrago() {
	}
	
	public static CastelloDrago getIstanza() {
		return istanza;
	}
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.CASTELLO_DRAGO;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		gng.aggiungiPersonaggio(new Drago());
	}

	@Override
	public String getNome() {
		return "il Castello della Morte Alata";
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		UI.notifica(g.chi() + " entra al Castello della Morte Alata, per lo scontro finale. Con uno sbatter d'ali simile al rombo di tuono, la Grande Minaccia si erge ora davanti al gruppo: il Drago e' qui!");
	}

	@Override
	public void azzeraLocazione(GruppoGiocatore g) {
		if (completa) {
			LineaTemporale.setDragoSconfitto(true);
			g.setLocazioneVisitata();
			Foresta.distruggiLocazioneUnica(getClasseLocazione(), ClassiLocazione.ROVINE);
		}
	}
}
