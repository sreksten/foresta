package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRiposo;

public class Radura extends LocazioneBase {

	@Override
	public TipoLocazione getTipoLocazione() {
		return TipoLocazione.RADURA;
	}

	private static final TipoPersonaggio[] mostri = {
			TipoPersonaggio.ARPIA,
			TipoPersonaggio.CENTAURO,
			TipoPersonaggio.CHIMERA,
			TipoPersonaggio.CHIMERA_DRAGO,
			TipoPersonaggio.EREMITA,
			TipoPersonaggio.FOLLETTO,
			TipoPersonaggio.GIGANTE,
			TipoPersonaggio.GOBLIN,
			TipoPersonaggio.HOBGOBLIN,
			TipoPersonaggio.MINOTAURO,
			TipoPersonaggio.SCHELETRO,
			TipoPersonaggio.TITANO,
			TipoPersonaggio.TROLL,
			TipoPersonaggio.VIVERNA
	};

	private static final TipoOggetto[] oggetti = {};

	@Override
	public TipoPersonaggio[] getPossibiliIncontri() {
		return mostri;
	}

	@Override
	public TipoOggetto[] getPossibiliOggetti() {
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
