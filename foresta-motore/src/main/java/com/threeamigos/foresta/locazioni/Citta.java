package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAggiornamentoComandiDisponibili;
import com.threeamigos.foresta.eventi.interni.InternoMostraFinestraStatistiche;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.modellodati.LocazioneMD;
import com.threeamigos.foresta.gruppi.GruppoAvversario;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.economia.CommercioDelGruppo;
import com.threeamigos.foresta.motore.Stato;
import com.threeamigos.foresta.testi.Articoli;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoRiposo;

/**
 * Una città: il gruppo vi arriva, vende le pietre preziose e può andare in piazza, alla locanda, dall'alchimista, dall'armaiolo, dal venditore
 * di pergamene o dall'incantatore. Non si combatte, e non si può riposare all'aperto. Le quattro città sono uniche e il drago le distrugge una dopo l'altra.
 * Le sottoclassi si distinguono solo per il tipo.
 */
public abstract class Citta extends LocazioneUnica {

	private enum StatoInCitta {
		IN_PIAZZA,
		IN_LOCANDA,
		IN_BOTTEGA
	}

	private StatoInCitta stato;
	// La locanda sta sulla casella della città: ne condivide il modello dati, così quel che vi segna resta lì.
	private Locanda locanda;
	// Le botteghe: l'alchimista, l'armaiolo, il venditore di pergamene e l'incantatore (vedi GestoreBotteghe)
	private final GestoreBotteghe botteghe = new GestoreBotteghe();

	protected Citta() {
		stato = StatoInCitta.IN_PIAZZA;
	}

	@Override
	public void setModelloDati(LocazioneMD modelloDati) {
		super.setModelloDati(modelloDati);
		if (locanda != null) {
			locanda.setModelloDati(modelloDati);
		}
	}

	private Locanda nuovaLocanda() {
		locanda = new Locanda();
		locanda.setModelloDati(getModelloDati());
		return locanda;
	}

	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
        BusEventi.pubblica(new NotificaTestoParagrafo(g.chiMaiuscolo() + " arriva " + Articoli.conPreposizione("a", getNome()) +
                ". Qui è possibile cercare una locanda, il negozio di un alchimista, fare un salto dall'armaiolo o dal venditore di pergamene, o far incantare un artefatto prima di andare via."));
		if (Cassa.getPreziosi() > 0) {
			CommercioDelGruppo.vendePreziosi(g);
			BusEventi.pubblica(new InternoMostraFinestraStatistiche());
		}
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		nuovaLocanda().crea(g, gng);
		setCompleta(true);
	}

	private void impostaAzioniCitta() {
		// I negozi in fila: locanda, alchimista, armaiolo, venditore di pergamene, incantatore
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.LOCANDA, Comando.NEGOZIO_ALCHIMISTA, Comando.NEGOZIO_ARMAIOLO,
				Comando.NEGOZIO_VENDITORE_DI_PERGAMENE, Comando.NEGOZIO_INCANTATORE, Comando.INVENTARIO, Comando.ESCI_DA_CITTA));
	}
	
	@Override
	public Stato impostaAzioni(GruppoGiocatore g, GruppoAvversario gng, Comando azione) {
		Stato statoRitorno;
		if (stato == StatoInCitta.IN_PIAZZA) {
			if (azione == null) {
				impostaAzioniCitta();

			} else if (azione == Comando.LOCANDA) {
				nuovaLocanda();
				locanda.descrivi(g, gng);
				statoRitorno = locanda.impostaAzioni(g, gng, null);
				if (statoRitorno == Stato.IN_LOCAZIONE) {
					stato = StatoInCitta.IN_LOCANDA;
				} else {
					stato = StatoInCitta.IN_PIAZZA;
					impostaAzioniCitta();
				}

			} else if (GestoreBotteghe.isComandoDiIngresso(azione)) {
				stato = StatoInCitta.IN_BOTTEGA;
				botteghe.apri(g, azione);

			} else if (azione == Comando.ESCI_DA_CITTA) {
				return Stato.FINE_LOCAZIONE;
			}

		} else if (stato == StatoInCitta.IN_LOCANDA) {
			statoRitorno = locanda.impostaAzioni(g, gng, azione);
			if (statoRitorno == Stato.FINE_LOCAZIONE) {
				impostaAzioniCitta();
				stato = StatoInCitta.IN_PIAZZA;
			}

		} else if (stato == StatoInCitta.IN_BOTTEGA) {
			if (botteghe.gestisci(g, azione)) {
				impostaAzioniCitta();
				stato = StatoInCitta.IN_PIAZZA;
			}
		}
		return Stato.IN_LOCAZIONE;
	}

	/**
	 * Il nome proprio dell'artefatto da fondere, dall'incantatore.
	 */
	@Override
	public Stato riceviTesto(GruppoGiocatore g, String testo) {
		botteghe.riceviTesto(testo);
		return Stato.IN_LOCAZIONE;
	}

	/**
	 * Mappa e inventario sono raggiungibili solo dalla piazza: dentro la locanda o
	 * dalle botteghe la città non offre quei comandi.
	 */
	@Override
	public void ripresentaComandi() {
		if (stato != StatoInCitta.IN_PIAZZA) {
			throw new IllegalStateException("Citta::ripresentaComandi(): la città non è in piazza ma in " + stato);
		}
		impostaAzioniCitta();
	}

	public TipoRiposo getTipoRiposo() {
		throw new IllegalArgumentException("Non si può riposare all'aperto in città");
	}
}
