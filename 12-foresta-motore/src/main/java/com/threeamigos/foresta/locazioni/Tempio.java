package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.missioni.principali.IlGiroDelleSetteChiese;
import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.oggetti.BottinoArtefatto;
import com.threeamigos.foresta.oggetti.Cofano;
import com.threeamigos.foresta.personaggi.FabbricaPersonaggi;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.testi.ProduttoreDiLuoghiCasuale;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoPersonaggio;
import com.threeamigos.foresta.tipi.TipoRiposo;

/**
 * Un tempio, con il suo nome (il Santuario della Luna): lo custodiscono delle viverne, e custodiscono un cofano oppure, se il registro ne ha uno da
 * dare, un artefatto.
 */
public class Tempio extends LocazioneBase {

	@Override
	public TipoLocazione getTipoLocazione() {
		return TipoLocazione.TEMPIO;
	}

	/**
	 * Il nome del tempio, con l'articolo: "il Santuario della Luna".
	 */
	@Override
	public String getNome() {
		return Foresta.nomeDelTempio(getModelloDati());
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		int nViverne;
        Artefatto a = getArtefatto(g);
		if (IlGiroDelleSetteChiese.isRipulito(getModelloDati())) {
			// Niente mostri né cofano; un artefatto, se c'è, si trova lì e il gruppo lo prende come in ogni locazione senza custodi
			if (a != null) {
				setOggetto(BottinoArtefatto.giaFatto(a));
			}
			return;
		}
		if (a == null) {
			nViverne = Dado.tira(5);
			if (!isLocazioneVisitata()) {
				setOggetto(new Cofano(Dado.tira(0, 5)));
			}
		} else {
			nViverne = Dado.tira(4, 7);
			setOggetto(BottinoArtefatto.giaFatto(a));
		}
		Personaggio p;
		for (int i = 0; i < nViverne; i++) {
			p = FabbricaPersonaggi.crea(TipoPersonaggio.VIVERNA, Statistiche.getLivello());
			gng.aggiungiPersonaggio(p);
		}
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		if (IlGiroDelleSetteChiese.isRipulito(getModelloDati()) && getOggetto() == null) {
			BusEventi.pubblica(new NotificaTestoFrase("Qui, " + Articoli.conPreposizione("in", getNome())
					+ ", regna la pace. Questo era uno dei templi ripuliti durante il Giro delle Sette Chiese."));
			return;
		}
		BusEventi.pubblica(new NotificaTestoFrase("Qui, " + Articoli.conPreposizione("in", getNome()) + ", " + descrizioneMostriEOggetti(g, gng)));
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_CON_FUOCO;
	}
}
