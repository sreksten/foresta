package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.tipi.TipoRiposo;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.tools.Misc;

public class Rovine extends LocazioneBase {

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.ROVINE;
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
			modelloDati.setNome(Foresta.nomeNuovo(ClassiLocazione.ROVINE, ProduttoreDiTestiCasuale::nomeRovine));
		}
		return modelloDati.getNome();
	}

	private static final ClassePersonaggio[] mostri = {
			ClassePersonaggio.CHIMERA,
			ClassePersonaggio.CHIMERA_DRAGO,
			ClassePersonaggio.FANTASMA,
			ClassePersonaggio.GARGOYLE,
			ClassePersonaggio.GOBLIN,
			ClassePersonaggio.HOBGOBLIN,
			ClassePersonaggio.MINOTAURO,
			ClassePersonaggio.OMBRA_NERA,
			ClassePersonaggio.SCHELETRO,
			ClassePersonaggio.SPETTRO,
			ClassePersonaggio.SPIRITO
	};

	private static final ClassiOggetto[] oggetti = {
			ClassiOggetto.COFANO
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
		BusEventi.pubblica(new NotificaTestoFrase("Qui, " + Misc.conPreposizione("in", getNome()) + ", " + descrizioneMostriEOggetti(g, gng)));
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_CON_FUOCO;
	}
}
