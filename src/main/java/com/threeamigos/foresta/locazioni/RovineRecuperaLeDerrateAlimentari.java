package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.RegistroMissioni.TipoMissione;
import com.threeamigos.foresta.personaggi.Troll;
import com.threeamigos.foresta.ui.UI;

public class RovineRecuperaLeDerrateAlimentari extends LocazioneUnica {

	private static RovineRecuperaLeDerrateAlimentari istanza = new RovineRecuperaLeDerrateAlimentari();
	
	private RovineRecuperaLeDerrateAlimentari() {
	}
	
	public static RovineRecuperaLeDerrateAlimentari getIstanza() {
		return istanza;
	}
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.ROVINE_RECUPERA_LE_DERRATE_ALIMENTARI;
	}

	@Override
	public String getNome() {
		return "il covo dei Troll che hanno rubato il carico di derrate alimentari";
	}	

	private final boolean isMissioneCompleta() {
		return RegistroMissioni.getMissione(TipoMissione.RECUPERA_LE_DERRATE_ALIMENTARI).isCompleta(); 
	}
	
	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		if (!isMissioneCompleta()) {
			for (int i = 0; i < 7; i++) {
				Troll troll = new Troll();
				troll.setCorrompibile(false);
				gng.aggiungiPersonaggio(troll);
			}
		} else {
			Rovine.getIstanza().crea(g, gng);
		}
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		if (!isMissioneCompleta()) {
			UI.notifica("Tra queste rovine si nasconde la banda di Troll che ha rubato il carico di derrate alimentari!");
		} else {
			UI.notifica("Tra queste rovine si nascondeva la banda di Troll che aveva rubato il carico di derrate alimentari.");
		}
	}

	@Override
	public void azzeraLocazione(GruppoGiocatore g) {
		if (completa) {
			g.setLocazioneVisitata();
		}
	}

}
