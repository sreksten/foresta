package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;
import com.threeamigos.foresta.oggetti.ClassiOggetto;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.ui.UI;

public class Rovine extends LocazioneBase {

	private static final Rovine istanza = new Rovine();
	
	private Rovine() {
	}
	
	public static Rovine getIstanza() {
		return istanza;
	}
	
	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.ROVINE;
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
		UI.notifica("Qui, in mezzo a delle rovine, " + descrizioneMostriEOggetti(g, gng));
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_CON_FUOCO;
	}
}
