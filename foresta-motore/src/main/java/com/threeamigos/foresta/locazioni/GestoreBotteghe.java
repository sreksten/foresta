package com.threeamigos.foresta.locazioni;

import com.threeamigos.foresta.economia.Cassa;
import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.artefatti.RegoleIncantatura;

import com.threeamigos.foresta.artefatti.BancoDiLavoro;

import com.threeamigos.foresta.artefatti.ScambiatoreArtefatti;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoAggiornamentoComandiDisponibili;
import com.threeamigos.foresta.eventi.interni.InternoIncantatura;
import com.threeamigos.foresta.eventi.interni.InternoMostraSchermataGioco;
import com.threeamigos.foresta.eventi.notifiche.NotificaRifiutoIncantatura;
import com.threeamigos.foresta.eventi.richieste.RichiestaAperturaIncantatore;
import com.threeamigos.foresta.eventi.richieste.RichiestaAperturaInventarioCommerciante;
import com.threeamigos.foresta.eventi.richieste.RichiestaAperturaInventarioFornitore;
import com.threeamigos.foresta.eventi.richieste.RichiestaTesto;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.motore.*;
import com.threeamigos.foresta.economia.MagazziniDeiNegozi;
import com.threeamigos.foresta.economia.CommercioDelGruppo;
import com.threeamigos.foresta.economia.OfferteAlchimista;
import com.threeamigos.foresta.tipi.Comando;
import com.threeamigos.foresta.tipi.TipoMotivoRifiutoIncantatura;
import com.threeamigos.foresta.tipi.TipoNegozio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Le quattro botteghe in cui il gruppo entra con un comando (alchimista, armaiolo, venditore di pergamene, incantatore):
 * quelle di una città (vedi Citta) e quelle sparse per la Foresta (vedi Bottega) funzionano allo stesso modo, e qui sta
 * quel che hanno in comune: aprire la finestra giusta, reggere la fusione dell'incantatore e chiudere la bottega.
 */
final class GestoreBotteghe {

	private enum StatoInBottega {
		NESSUNA,
		DA_ALCHIMISTA,
		DA_ARMAIOLO,
		DA_VENDITORE_DI_PERGAMENE,
		DA_INCANTATORE,
		// Dall'incantatore, in attesa del nome proprio dell'artefatto da fondere
		NOME_ARTEFATTO_DA_FONDERE
	}

	private StatoInBottega stato = StatoInBottega.NESSUNA;
	// La bottega dell'incantatore: il banco di lavoro vive solo finché si è dentro
	private AutomaIncantatore incantatore;

	/**
	 * Se il comando è quello con cui si entra in una bottega.
	 */
	static boolean isComandoDiIngresso(Comando comando) {
		return comando == Comando.NEGOZIO_ALCHIMISTA || comando == Comando.NEGOZIO_ARMAIOLO
				|| comando == Comando.NEGOZIO_VENDITORE_DI_PERGAMENE || comando == Comando.NEGOZIO_INCANTATORE;
	}

	/**
	 * Se il gruppo è dentro una bottega.
	 */
	boolean isAperta() {
		return stato != StatoInBottega.NESSUNA;
	}

	/**
	 * Entra nella bottega del comando e ne apre la finestra.
	 */
	void apri(GruppoGiocatore g, Comando ingresso) {
		switch (ingresso) {
			case NEGOZIO_ALCHIMISTA:
				stato = StatoInBottega.DA_ALCHIMISTA;
				List<Comando> comandiPossibili = new ArrayList<>();
				comandiPossibili.add(Comando.ANNULLA);
				BusEventi.pubblica(new RichiestaAperturaInventarioFornitore(comandiPossibili, OfferteAlchimista.elenco(g)));
				break;
			case NEGOZIO_ARMAIOLO:
				stato = StatoInBottega.DA_ARMAIOLO;
				apriNegozio(g, TipoNegozio.ARMAIOLO);
				break;
			case NEGOZIO_VENDITORE_DI_PERGAMENE:
				stato = StatoInBottega.DA_VENDITORE_DI_PERGAMENE;
				apriNegozio(g, TipoNegozio.VENDITORE_DI_PERGAMENE);
				break;
			case NEGOZIO_INCANTATORE:
				stato = StatoInBottega.DA_INCANTATORE;
				incantatore = new AutomaIncantatore(g, new BancoDiLavoro());
				apriIncantatore("Mettete sul banco un artefatto e gli ingredienti magici da fondere.");
				break;
			default:
				throw new IllegalArgumentException(ingresso + " non è l'ingresso di una bottega");
		}
	}

	/**
	 * Un comando del giocatore dentro la bottega: la fusione dell'incantatore, o l'uscita.
	 *
	 * @return true se la bottega si è chiusa
	 */
	boolean gestisci(GruppoGiocatore g, Comando azione) {
		if (stato == StatoInBottega.DA_INCANTATORE) {
			if (azione == Comando.FUSIONE) {
				chiediNomeArtefattoDaFondere(g);
			} else if (azione == Comando.ANNULLA) {
				// Quel che resta sul banco torna nel gruppo: il banco non si salva
				incantatore.svuotaBanco();
				incantatore = null;
				chiudi();
				return true;
			}
		} else if (stato != StatoInBottega.NESSUNA && stato != StatoInBottega.NOME_ARTEFATTO_DA_FONDERE && azione == Comando.ANNULLA) {
			chiudi();
			return true;
		}
		return false;
	}

	/**
	 * Il nome proprio dell'artefatto da fondere: si fa la fusione e si torna nella bottega.
	 */
	void riceviTesto(String testo) {
		if (stato != StatoInBottega.NOME_ARTEFATTO_DA_FONDERE) {
			return;
		}
		stato = StatoInBottega.DA_INCANTATORE;
		BusEventi.pubblica(new InternoIncantatura(incantatore.getBanco(), testo));
		apriIncantatore(null);
	}

	private void chiudi() {
		stato = StatoInBottega.NESSUNA;
		BusEventi.pubblica(new InternoMostraSchermataGioco());
	}

	private void apriNegozio(GruppoGiocatore g, TipoNegozio negozio) {
		List<Comando> comandiPossibili = new ArrayList<>();
		comandiPossibili.add(Comando.ANNULLA);
		ScambiatoreArtefatti magazzino = MagazziniDeiNegozi.getScambiatorePerNegozio(Foresta.getPosizioneDelGruppo(), negozio);
		BusEventi.pubblica(new RichiestaAperturaInventarioCommerciante(comandiPossibili, negozio,
				new AutomaAcquistiArtefatti(g, magazzino)));
	}

	private void apriIncantatore(String messaggio) {
		List<Comando> comandiPossibili = new ArrayList<>();
		comandiPossibili.add(Comando.FUSIONE);
		comandiPossibili.add(Comando.ANNULLA);
		BusEventi.pubblica(new RichiestaAperturaIncantatore(comandiPossibili, incantatore, messaggio));
	}

	/**
	 * Se sul banco la fusione si può fare, chiede il nome proprio dell'artefatto, proponendo quello
	 * che ha già; altrimenti l'incantatore dice perché no.
	 */
	private void chiediNomeArtefattoDaFondere(GruppoGiocatore g) {
		Collection<Artefatto> banco = incantatore.getBanco().getInventario();
		Optional<TipoMotivoRifiutoIncantatura> motivo = RegoleIncantatura.verifica(banco, Cassa.getMonete(), CommercioDelGruppo.costoFusione(g, banco), Statistiche.getLivello());
		if (motivo.isPresent()) {
			BusEventi.pubblica(new NotificaRifiutoIncantatura(motivo.get()));
			return;
		}
		if (RegoleIncantatura.conRiprogrammazione(banco)) {
			// L'artefatto non cambia nome: si riprogramma subito, e l'incantatore dice com'è andata
			BusEventi.pubblica(new InternoIncantatura(incantatore.getBanco(), ""));
			apriIncantatore(null);
			return;
		}
		String nomeAttuale = RegoleIncantatura.artefattoSulBanco(banco)
				.flatMap(Artefatto::getNomeProprio)
				.orElse("");
		stato = StatoInBottega.NOME_ARTEFATTO_DA_FONDERE;
		BusEventi.pubblica(new InternoAggiornamentoComandiDisponibili());
		// Il messaggio si scrive in grande: niente accenti né parentesi
		BusEventi.pubblica(new RichiestaTesto("Come si chiamera' l'artefatto? La fusione costa "
				+ CommercioDelGruppo.costoFusione(g, banco) + " monete.", nomeAttuale));
	}
}
