package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraStato;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoFrase;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.motore.Stato;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoLocazione;
import com.threeamigos.foresta.tipi.TipoRiposo;

/**
 * Una palude malsana: da sola è vuota, e avversari o oggetti ce li mette solo una missione. Altrimenti il gruppo, sette volte su dieci, decide di non fermarsi;
 * le altre approfitta della quiete per riposare un poco, all'aperto e senza fuoco.
 */
public class Palude extends LocazioneBase {

	@Override
	public TipoLocazione getTipoLocazione() {
		return TipoLocazione.PALUDE;
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		// niente da creare
	}

	/**
	 * Da sola la palude è vuota: avversari o oggetti ce li mette solo una missione (vedi
	 * RegistroMissioni.getIncontroMissione e getOggettoMissione), e allora si va avanti come in ogni altra locazione.
	 */
	private boolean isOccupataDaUnaMissione(GruppoAvversario gng) {
		return gng.getNumeroPersonaggi() > 0 || getOggetto() != null;
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
		if (isOccupataDaUnaMissione(gng)) {
			BusEventi.pubblica(new NotificaTestoFrase("Qui, nel mezzo di una malsana e pericolosa palude, " + descrizioneMostriEOggetti(g, gng)));
			return;
		}
		BusEventi.pubblica(new NotificaTestoFrase("Qui, nel mezzo di una malsana e pericolosa palude, " + g.chi() + " non trova nulla."));
	}

	@Override
	public Stato impostaAzioni(GruppoGiocatore g, GruppoAvversario gng, Comando azione) {
		if (isOccupataDaUnaMissione(gng)) {
			return super.impostaAzioni(g, gng, azione);
		}
		if (isLocazioneVisitata() || Dado.tira(10) > 3) {
			BusEventi.pubblica(new NotificaTestoFrase("Il posto però non promette nulla di buono e " + g.chi() + " decide di non restare."));
			setCompleta(true);
			return Stato.FINE_LOCAZIONE;
		}
		BusEventi.pubblica(new NotificaTestoFrase("Approfittando della quiete del posto, " + g.chi() + " riposerà un poco."));
		g.riposa(1, getTipoRiposo());
		BusEventi.pubblica(new InternoMostraFinestraStato());
		setCompleta(true);
		return Stato.FINE_LOCAZIONE;
	}

	public TipoRiposo getTipoRiposo() {
		return TipoRiposo.ALL_APERTO_SENZA_FUOCO;
	}
}
