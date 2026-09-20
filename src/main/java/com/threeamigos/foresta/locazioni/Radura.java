package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;

public class Radura extends LocazioneBase {

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.RADURA;
	}

	private static final ClassePersonaggio[] mostri = {
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

	private static final ClassiOggetto[] oggetti = {};

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
		BusEventi.pubblica(new NotificaTestoFrase("Qui, in una radura, " + descrizioneMostriEOggetti(g, gng)));
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_CON_FUOCO;
	}
}
