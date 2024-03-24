package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.RegistroMissioni;
import com.threeamigos.foresta.motore.RegistroMissioni.TipoMissione;
import com.threeamigos.foresta.personaggi.Ladra;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.ui.UI;

public class GrottaRecuperaIlMedaglione extends LocazioneUnica {

	private static GrottaRecuperaIlMedaglione istanza = new GrottaRecuperaIlMedaglione();
	
	private GrottaRecuperaIlMedaglione() {
	}
	
	public static GrottaRecuperaIlMedaglione getIstanza() {
		return istanza;
	}
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.GROTTA_RECUPERA_IL_MEDAGLIONE;
	}

	@Override
	public String getNome() {
		return "la grotta dei ladri del Medaglione";
	}
	
	private final boolean isMissioneCompleta() {
		return RegistroMissioni.getMissione(TipoMissione.RECUPERA_IL_MEDAGLIONE).isCompleta(); 
	}
	
	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		if (!isMissioneCompleta()) {
			Ladro ladro = new Ladro();
			ladro.setAmichevole(false);
			ladro.setCorrompibile(false);
			gng.aggiungiPersonaggio(ladro);
			Ladra ladra = new Ladra();
			ladra.setAmichevole(false);
			ladra.setCorrompibile(false);
			gng.aggiungiPersonaggio(ladra);
			ladro = new Ladro();
			ladro.setAmichevole(false);
			ladro.setCorrompibile(false);
			gng.aggiungiPersonaggio(ladro);
			ladro = new Ladro();
			ladro.setAmichevole(false);
			ladro.setCorrompibile(false);
			gng.aggiungiPersonaggio(ladro);
		} else {
			Grotta.getIstanza().crea(g, gng);
		}
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		if (!isMissioneCompleta()) {
			UI.notifica("Questa e' la grotta dove risiede la banda di ladri che ha rubato il Medaglione!");
		} else {
			UI.notifica("In questa grotta avevano il loro covo i ladri del medaglione.");
		}
	}
	
	@Override
	public void azzeraLocazione(GruppoGiocatore g) {
		if (completa) {
			g.setLocazioneVisitata();
			Foresta.impostaLocazioneCorrente(ClassiLocazione.GROTTA);
		}
	}

}
