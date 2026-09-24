package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoMostraSchermataGioco;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaIncantatore;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaInventarioCommerciante;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoIncantatura;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoIncantatura;
import com.threeamigos.foresta.eventi.richieste.RichiestaTesto;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAperturaInventarioFornitore;
import com.threeamigos.foresta.eventi.interni.InternoAggiornamentoComandiDisponibili;
import com.threeamigos.foresta.eventi.notifiche.NotificaTestoParagrafo;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.motore.modellodati.LocazioneMD;
import com.threeamigos.foresta.motore.modellodati.TipoRiposo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class Citta extends LocazioneUnica {

	private enum StatoInCitta {
		IN_PIAZZA,
		IN_LOCANDA,
		DA_ALCHIMISTA,
		DA_ARMAIOLO,
		DA_INCANTATORE,
		// Dall'incantatore, in attesa del nome proprio dell'artefatto da fondere
		NOME_ARTEFATTO_DA_FONDERE
	}

	private StatoInCitta stato;
	// La locanda e la bottega dell'alchimista stanno sulla casella della città:
	// ne condividono il modello dati, così quel che vi segnano resta lì.
	private Locanda locanda;
	private Alchimista alchimista;
	// La bottega dell'incantatore: il banco di lavoro vive solo finché si è dentro
	private AutomaIncantatore incantatore;

	protected Citta() {
		stato = StatoInCitta.IN_PIAZZA;
	}

	@Override
	public void setModelloDati(LocazioneMD modelloDati) {
		super.setModelloDati(modelloDati);
		if (locanda != null) {
			locanda.setModelloDati(modelloDati);
		}
		if (alchimista != null) {
			alchimista.setModelloDati(modelloDati);
		}
	}

	private Locanda nuovaLocanda() {
		locanda = new Locanda();
		locanda.setModelloDati(getModelloDati());
		return locanda;
	}

	private Alchimista nuovoAlchimista() {
		alchimista = new Alchimista();
		alchimista.setModelloDati(getModelloDati());
		return alchimista;
	}

	public abstract String getNome();
	
	@Override
	public void descrivi(GruppoGiocatore g, GruppoAvversario gng) {
        BusEventi.pubblica(new NotificaTestoParagrafo(g.chiMaiuscolo() + " arriva al" + getNome() +
                ". Qui è possibile cercare una locanda, il negozio di un alchimista, fare un salto dall'armaiolo o far incantare un artefatto prima di andare via."));
		if (g.getPreziosi() > 0) {
			g.vendePreziosi();
		}
	}

	@Override
	public void crea(GruppoGiocatore g, GruppoAvversario gng) {
		nuovaLocanda().crea(g, gng);
		nuovoAlchimista().crea(g, gng);
		setCompleta(true);
	}

	private void impostaAzioniCitta() {
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili(Comando.LOCANDA, Comando.ALCHIMISTA, Comando.ARMAIOLO,
				Comando.FUSIONE, Comando.INVENTARIO, Comando.ESCI_DA_CITTA));
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

			} else if (azione == Comando.ALCHIMISTA) {
				stato = StatoInCitta.DA_ALCHIMISTA;
				List<Comando> comandiPossibili = new ArrayList<>();
				comandiPossibili.add(Comando.ANNULLA);
				BusEventi.pubblica(new ComandoAperturaInventarioFornitore(comandiPossibili));

			} else if (azione == Comando.ARMAIOLO) {
				stato = StatoInCitta.DA_ARMAIOLO;
				List<Comando> comandiPossibili = new ArrayList<>();
				comandiPossibili.add(Comando.ANNULLA);
				ScambiatoreArtefatti scambiatoreArtefatti = RegistroArtefatti.getScambiatorePerLocazione(g.getCoordinate());
				BusEventi.pubblica(new ComandoAperturaInventarioCommerciante(comandiPossibili,
						new AutomaAcquistiArtefatti(g, scambiatoreArtefatti)));

			} else if (azione == Comando.FUSIONE) {
				stato = StatoInCitta.DA_INCANTATORE;
				incantatore = new AutomaIncantatore(g, new BancoDiLavoro());
				apriIncantatore("Benvenuti. Mettete sul banco un artefatto e le pergamene da fondere.");

			} else if (azione == Comando.ESCI_DA_CITTA) {
				return Stato.FINE_LOCAZIONE;
			}

		} else if (stato == StatoInCitta.IN_LOCANDA) {
			statoRitorno = locanda.impostaAzioni(g, gng, azione);
			if (statoRitorno == Stato.FINE_LOCAZIONE) {
				impostaAzioniCitta();
				stato = StatoInCitta.IN_PIAZZA;
			}

		} else if (stato == StatoInCitta.DA_ALCHIMISTA || stato == StatoInCitta.DA_ARMAIOLO) {
			if (azione == Comando.ANNULLA) {
				BusEventi.pubblica(new InternoMostraSchermataGioco());
				impostaAzioniCitta();
				stato = StatoInCitta.IN_PIAZZA;
			}

		} else if (stato == StatoInCitta.DA_INCANTATORE) {
			if (azione == Comando.FUSIONE) {
				chiediNomeArtefattoDaFondere(g);
			} else if (azione == Comando.ANNULLA) {
				// Quel che resta sul banco torna nel gruppo: il banco non si salva
				incantatore.svuotaBanco();
				incantatore = null;
				BusEventi.pubblica(new InternoMostraSchermataGioco());
				impostaAzioniCitta();
				stato = StatoInCitta.IN_PIAZZA;
			}
		}
		return Stato.IN_LOCAZIONE;
	}

	private void apriIncantatore(String messaggio) {
		List<Comando> comandiPossibili = new ArrayList<>();
		comandiPossibili.add(Comando.FUSIONE);
		comandiPossibili.add(Comando.ANNULLA);
		BusEventi.pubblica(new ComandoAperturaIncantatore(comandiPossibili, incantatore, messaggio));
	}

	/**
	 * Se sul banco la fusione si può fare, chiede il nome proprio dell'artefatto, proponendo quello
	 * che ha già; altrimenti l'incantatore dice perché no.
	 */
	private void chiediNomeArtefattoDaFondere(GruppoGiocatore g) {
		Collection<Artefatto> banco = incantatore.getBanco().getInventario();
		Optional<MotivoRifiutoIncantatura> motivo = RegoleIncantatura.verifica(banco, g.getMonete());
		if (motivo.isPresent()) {
			BusEventi.pubblica(new NotificaRifiutoIncantatura(motivo.get()));
			return;
		}
		String nomeAttuale = RegoleIncantatura.artefattoSulBanco(banco)
				.flatMap(Artefatto::getNomeProprio)
				.orElse("");
		stato = StatoInCitta.NOME_ARTEFATTO_DA_FONDERE;
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili());
		// Il messaggio si scrive in grande: niente accenti né parentesi
		BusEventi.pubblica(new RichiestaTesto("Come si chiamera' l'artefatto? La fusione costa "
				+ RegoleIncantatura.costo(banco) + " monete.", nomeAttuale));
	}

	/**
	 * Il nome proprio dell'artefatto da fondere: si fa la fusione e si torna nella bottega.
	 */
	@Override
	public Stato riceviTesto(GruppoGiocatore g, String testo) {
		if (stato != StatoInCitta.NOME_ARTEFATTO_DA_FONDERE) {
			return Stato.IN_LOCAZIONE;
		}
		stato = StatoInCitta.DA_INCANTATORE;
		BusEventi.pubblica(new ComandoIncantatura(incantatore.getBanco(), testo));
		apriIncantatore(null);
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
