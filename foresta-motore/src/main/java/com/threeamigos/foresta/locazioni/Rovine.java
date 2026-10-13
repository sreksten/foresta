package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoOggetto;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRiposo;
import com.threeamigos.foresta.strumenti.Misc;

public class Rovine extends LocazioneBase {

	@Override
	public TipoLocazione getTipoLocazione() {
		return TipoLocazione.ROVINE;
	}

	/**
	 * Il nome delle rovine, con l'articolo: "le Vestigia dell'Antico Impero", "le Rovine del Maniero del Malefizio".
	 */
	@Override
	public String getNome() {
		return getNome(getModelloDati());
	}

	/**
	 * Il nome delle rovine, con l'articolo. Le rovine di un castello o di una città lo ricevono quando nascono (vedi
	 * Foresta.distruggiLocazioneUnica); le altre, la prima volta che serve, lo pescano dalla grammatica (rovine.txt),
	 * possibilmente diverso da quello delle altre rovine, e lo salvano con la casella.
	 */
	public static String getNome(LocazioneMD modelloDati) {
		if (modelloDati.getNome() == null) {
			modelloDati.setNome(Foresta.nomeNuovo(TipoLocazione.ROVINE, ProduttoreDiTestiCasuale::nomeRovine));
		}
		return modelloDati.getNome();
	}

	private static final TipoPersonaggio[] mostri = {
			TipoPersonaggio.CHIMERA,
			TipoPersonaggio.CHIMERA_DRAGO,
			TipoPersonaggio.FANTASMA,
			TipoPersonaggio.GARGOYLE,
			TipoPersonaggio.GOBLIN,
			TipoPersonaggio.HOBGOBLIN,
			TipoPersonaggio.MINOTAURO,
			TipoPersonaggio.OMBRA_NERA,
			TipoPersonaggio.SCHELETRO,
			TipoPersonaggio.SPETTRO,
			TipoPersonaggio.SPIRITO
	};

	private static final TipoOggetto[] oggetti = {
			TipoOggetto.COFANO
	};

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
		BusEventi.pubblica(new NotificaTestoFrase("Qui, " + Misc.conPreposizione("in", getNome()) + ", " + descrizioneMostriEOggetti(g, gng)));
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_CON_FUOCO;
	}
}
