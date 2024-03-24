package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.personaggi.ClassiPersonaggio;
import com.threeamigos.foresta.ui.UI;

public class Rovine extends LocazioneBase {

	private static Rovine istanza = new Rovine();
	
	private Rovine() {
	}
	
	public static Rovine getIstanza() {
		return istanza;
	}
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.ROVINE;
	}

	private static ClassiPersonaggio[] mostri = {
			ClassiPersonaggio.CHIMERA,
			ClassiPersonaggio.CHIMERA_DRAGO,
			ClassiPersonaggio.FANTASMA,
			ClassiPersonaggio.GARGOYLE,
			ClassiPersonaggio.GOBLIN,
			ClassiPersonaggio.HOBGOBLIN,
			ClassiPersonaggio.MINOTAURO,
			ClassiPersonaggio.OMBRA_NERA,
			ClassiPersonaggio.SCHELETRO,
			ClassiPersonaggio.SPETTRO,
			ClassiPersonaggio.SPIRITO
	};

	private static ClassiOggetto[] oggetti = {
			ClassiOggetto.COFANO
	};

	@Override
	public ClassiPersonaggio[] getPossibiliIncontri() {
		return mostri;
	}

	@Override
	public ClassiOggetto[] getPossibiliOggetti() {
		return oggetti;
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		UI.notifica("Qui, in mezzo a delle rovine, " + descrizioneMostriEOggetti(g, gng));
	}
}
