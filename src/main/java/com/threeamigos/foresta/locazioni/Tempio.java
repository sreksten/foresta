package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoAvversario;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.ProduttoreDiTestiCasuale;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.ClassePersonaggio;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoRiposo;
import com.threeamigos.foresta.tools.Misc;

public class Tempio extends LocazioneBase {

	@Override
	public ClassiLocazione getClasseLocazione() {
		return ClassiLocazione.TEMPIO;
	}

	/**
	 * Il nome del tempio, con l'articolo: "il Santuario della Luna".
	 */
	@Override
	public String getNome() {
		return getNome(getModelloDati());
	}

	/**
	 * Il nome del tempio in quelle coordinate (vedi {@link #getNome(LocazioneMD)}).
	 */
	public static String getNome(CoordinateMD coordinate) {
		return getNome(Foresta.getLocazioneMD(coordinate));
	}

	/**
	 * Il nome del tempio, con l'articolo. La prima volta che serve si pesca dalla grammatica (templi.txt), possibilmente
	 * diverso da quello degli altri templi, e si salva con la casella: da lì in poi è sempre quello, anche dopo un
	 * caricamento.
	 */
	public static String getNome(LocazioneMD modelloDati) {
		if (modelloDati.getNome() == null) {
			modelloDati.setNome(Foresta.nomeNuovo(ClassiLocazione.TEMPIO, ProduttoreDiTestiCasuale::nomeTempio));
		}
		return modelloDati.getNome();
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		int nViverne;
        Artefatto a = getArtefatto(g);
		if (a == null) {
			nViverne = Dado.tira(5);
			if (!isLocazioneVisitata()) {
				setOggetto(new Cofano(Dado.tira(0, 5)));
			}
		} else {
			nViverne = Dado.tira(4, 7);
			setOggetto(a);
		}
		Personaggio p;
		for (int i = 0; i < nViverne; i++) {
			p = ClassePersonaggio.VIVERNA.getIstanza(Statistiche.getLivello());
			p.setOrdinale(i + 1);
			gng.aggiungiPersonaggio(p);
		}
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		BusEventi.pubblica(new NotificaTestoFrase("Qui, " + Misc.conPreposizione("in", getNome()) + ", " + descrizioneMostriEOggetti(g, gng)));
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_CON_FUOCO;
	}
}
