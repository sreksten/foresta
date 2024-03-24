package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.personaggi.ClassiPersonaggio;
import com.threeamigos.foresta.ui.UI;

public class Bosco extends LocazioneBase {

	private static Bosco istanza = new Bosco();
	
	private Bosco() {
	}
	
	public static Bosco getIstanza() {
		return istanza;
	}
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.BOSCO;
	}

	private static ClassiPersonaggio[] mostri = {
			ClassiPersonaggio.ARPIA,
			ClassiPersonaggio.CENTAURO,
			ClassiPersonaggio.CHIMERA,
			ClassiPersonaggio.CHIMERA_DRAGO,
			ClassiPersonaggio.EREMITA,
			ClassiPersonaggio.FOLLETTO,
			ClassiPersonaggio.GIGANTE,
			ClassiPersonaggio.GOBLIN,
			ClassiPersonaggio.HOBGOBLIN,
			ClassiPersonaggio.MINOTAURO,
			ClassiPersonaggio.SCHELETRO,
			ClassiPersonaggio.TITANO,
			ClassiPersonaggio.TROLL,
			ClassiPersonaggio.VIVERNA
	};

	private static ClassiOggetto[] oggetti = {
			ClassiOggetto.ANELLO,
			ClassiOggetto.COFANO,
			ClassiOggetto.CORONA,
			ClassiOggetto.GEMMA,
			ClassiOggetto.MONETA,
			ClassiOggetto.SCUDO,
			ClassiOggetto.SPADA
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
		UI.notifica("Qui, nella foresta, " + descrizioneMostriEOggetti(g, gng));
	}
}
