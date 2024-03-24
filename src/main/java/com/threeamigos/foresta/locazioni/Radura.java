package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.personaggi.ClassiPersonaggio;
import com.threeamigos.foresta.ui.UI;

public class Radura extends LocazioneBase {

	private static Radura istanza = new Radura();
	
	private Radura() {
	}
	
	public static Radura getIstanza() {
		return istanza;
	}
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.RADURA;
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

	private static ClassiOggetto[] oggetti = {};

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
		UI.notifica("Qui, in una radura, " + descrizioneMostriEOggetti(g, gng));
	}
}
