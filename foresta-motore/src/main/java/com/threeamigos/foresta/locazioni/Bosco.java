package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.mappa.LocazioneMD;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoBottino;
import com.threeamigos.foresta.mappa.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRiposo;

/**
 * Il bosco: la locazione più comune della Foresta. Vi si incontrano molte specie di mostri e vi si trovano anelli, cofani, monete, pietre preziose e
 * pezzi di equipaggiamento. Sulla mappa ha una fra più immagini alternative, scelta una volta per tutte quando la casella nasce.
 */
public class Bosco extends LocazioneBase {

	@Override
	public TipoLocazione getTipoLocazione() {
		return TipoLocazione.BOSCO;
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

	private static final TipoBottino[] oggetti = {
			TipoBottino.ANELLO,
			TipoBottino.COFANO,
			TipoBottino.CORONA,
			TipoBottino.PIETRA_PREZIOSA,
			TipoBottino.MONETA,
			TipoBottino.SCUDO,
			TipoBottino.SPADA,
			TipoBottino.SPADONE,
			TipoBottino.MAZZA,
			TipoBottino.ASCIA,
			TipoBottino.LANCIA,
			TipoBottino.BASTONE_MAGICO,
			// Il libro magico no: nel bosco se ne troverebbero troppi, viene dai cofani (l'artefatto casuale)
			TipoBottino.ARMATURA,
			TipoBottino.ELMO,
			TipoBottino.MASCHERA,
			TipoBottino.SCHINIERI
	};

	@Override
	public TipoPersonaggio[] getPossibiliIncontri() {
		return mostri;
	}

	@Override
	public TipoBottino[] getPossibiliOggetti() {
		return oggetti;
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		BusEventi.pubblica(new NotificaTestoFrase("Qui, nella foresta, " + descrizioneMostriEOggetti(g, gng)));
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_CON_FUOCO;
	}
}
