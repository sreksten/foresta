package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
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

	private static ClassePersonaggio[] mostri = {
			ClassePersonaggio.ARPIA,
			ClassePersonaggio.CENTAURO,
			ClassePersonaggio.CHIMERA,
			ClassePersonaggio.CHIMERA_DRAGO,
			ClassePersonaggio.EREMITA,
			ClassePersonaggio.FOLLETTO,
			ClassePersonaggio.GIGANTE,
			ClassePersonaggio.GOBLIN,
			ClassePersonaggio.HOBGOBLIN,
			ClassePersonaggio.MINOTAURO,
			ClassePersonaggio.SCHELETRO,
			ClassePersonaggio.TITANO,
			ClassePersonaggio.TROLL,
			ClassePersonaggio.VIVERNA
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
	public ClassePersonaggio[] getPossibiliIncontri() {
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
