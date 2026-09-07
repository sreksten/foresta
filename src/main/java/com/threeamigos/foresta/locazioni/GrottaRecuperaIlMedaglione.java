package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.RegistroMissioni.TipoMissionePredefinita;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.personaggi.Ladra;
import com.threeamigos.foresta.personaggi.Ladro;
import com.threeamigos.foresta.ui.UI;

public class GrottaRecuperaIlMedaglione extends LocazioneUnica {

	private static final GrottaRecuperaIlMedaglione istanza = new GrottaRecuperaIlMedaglione();
	
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
	
	private boolean isMissioneCompleta() {
		return RegistroMissioni.getMissione(TipoMissionePredefinita.RECUPERA_IL_MEDAGLIONE).isCompleta();
	}
	
	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		if (!isMissioneCompleta()) {
			int livello = Statistiche.getLivello();
			Ladro ladro = new Ladro(livello);
			ladro.setAmichevole(false);
			ladro.setCorrompibile(false);
			gng.aggiungiPersonaggio(ladro);
			Ladra ladra = new Ladra(livello + 1);
			ladra.setAmichevole(false);
			ladra.setCorrompibile(false);
			gng.aggiungiPersonaggio(ladra);
			ladro = new Ladro(Math.min(1, livello - 1));
			ladro.setAmichevole(false);
			ladro.setCorrompibile(false);
			gng.aggiungiPersonaggio(ladro);
			ladro = new Ladro(livello);
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
			UI.notifica("Questa è la grotta dove risiede la banda di ladri che ha rubato il Medaglione!");
		} else {
			UI.notifica("In questa grotta avevano il loro covo i ladri del medaglione.");
		}
	}
	
	@Override
	public void azzeraLocazione(GruppoGiocatore g) {
		if (completa) {
			g.setLocazioneCorrenteVisitata();
			Foresta.impostaLocazioneCorrente(ClassiLocazione.GROTTA);
		}
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_CON_FUOCO;
	}

}
