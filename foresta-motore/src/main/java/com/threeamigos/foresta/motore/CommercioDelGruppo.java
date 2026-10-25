package com.threeamigos.foresta.motore;

import com.threeamigos.foresta.statistiche.Statistiche;
import com.threeamigos.foresta.gruppi.GruppoGiocatore;
import com.threeamigos.foresta.tipi.CostantiPersonaggi;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.artefatti.BancoDiLavoro;
import com.threeamigos.foresta.artefatti.ProduttoreDiArtefattiCasuale;
import com.threeamigos.foresta.artefatti.RegoleIncantatura;
import com.threeamigos.foresta.tipi.TipoMotivoRifiutoIncantatura;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.comandigiocatore.ComandoAcquistoConsumabile;
import com.threeamigos.foresta.eventi.interni.InternoAcquistoArtefatto;
import com.threeamigos.foresta.eventi.interni.InternoIncantatura;
import com.threeamigos.foresta.eventi.interni.InternoVenditaArtefatto;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.mappa.Foresta;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Il commercio del gruppo del giocatore con il mondo: quanto paga e quanto ricava (la contrattazione), l'acquisto e la vendita di artefatti,
 * l'acquisto dall'alchimista, la fusione dall'incantatore e la vendita dei preziosi. Il gruppo sa solo di avere monete, artefatti e
 * scorte; i prezzi, le botteghe e il livello del mondo sono di qui. Gli eventi del commercio si ascoltano dopo {@link #registrati()}.
 */
public final class CommercioDelGruppo {

	private CommercioDelGruppo() {
	}

	/**
	 * Si iscrive agli eventi del commercio: l'acquisto e la vendita di artefatti, la fusione e l'acquisto di consumabili.
	 */
	public static void registrati() {
		BusEventi.iscriviti(InternoAcquistoArtefatto.class, CommercioDelGruppo::suEventoRichiestaAcquistoArtefatto);
		BusEventi.iscriviti(InternoVenditaArtefatto.class, CommercioDelGruppo::suEventoRichiestaVenditaArtefatto);
		BusEventi.iscriviti(InternoIncantatura.class, CommercioDelGruppo::suEventoIncantatura);
		BusEventi.iscriviti(ComandoAcquistoConsumabile.class, CommercioDelGruppo::suEventoRichiestaAcquistoConsumabile);
	}

	private static GruppoGiocatore gruppo() {
		return GruppoGiocatore.getIstanza();
	}

	// ---- I prezzi

	/**
	 * Quanto paga il gruppo un oggetto di quel costo (vedi RegoleContrattazione).
	 */
	public static int prezzoAcquisto(GruppoGiocatore gruppo, int costo) {
		return RegoleContrattazione.prezzoAcquisto(costo, gruppo.getContrattazione());
	}

	/**
	 * Quanto ricava il gruppo vendendo un oggetto di quel costo (vedi RegoleContrattazione).
	 */
	public static int prezzoVendita(GruppoGiocatore gruppo, int costo) {
		return RegoleContrattazione.prezzoVendita(costo, gruppo.getContrattazione());
	}

	/**
	 * Il costo della fusione di quel che c'è sul banco, scontato secondo la contrattazione del gruppo.
	 */
	public static int costoFusione(GruppoGiocatore gruppo, Collection<Artefatto> banco) {
		return prezzoAcquisto(gruppo, RegoleIncantatura.costo(banco));
	}

	/**
	 * Il costo di una fusione che trasferisce quel numero di effetti, scontato secondo la contrattazione del gruppo.
	 */
	public static int costoFusione(GruppoGiocatore gruppo, int effettiDaTrasferire) {
		return prezzoAcquisto(gruppo, RegoleIncantatura.costo(effettiDaTrasferire));
	}

	// ---- I preziosi

	/**
	 * Un prezioso vale quanto il livello del mondo, così le pietre e le corone seguono i prezzi che salgono col livello;
	 * la vendita ha miglior successo se nel gruppo c'è un ladro
	 */
	public static void vendePreziosi(GruppoGiocatore gruppo) {
		int preziosi = gruppo.getPreziosi();
		if (preziosi > 0) {
			int valore = Statistiche.getLivello();
			int quantita = preziosi * valore;
			if (gruppo.getPersonaggiVivi().stream().anyMatch(p -> p.getClasse() == TipoPersonaggio.LADRA || p.getClasse() == TipoPersonaggio.LADRO)) {
				quantita += Dado.tiraAncheAUnaFaccia(preziosi) * valore;
			}
			String notifica = gruppo.chiMaiuscolo() +
					" ha venduto i preziosi raccolti, ricavandone " +
					(quantita == 1 ? " una moneta." : (quantita + " monete."));
			BusEventi.pubblica(new NotificaTestoFrase(notifica));
			gruppo.vendiPreziosiPer(quantita);
		}
	}

	// ---- Gli artefatti

	private static void suEventoRichiestaAcquistoArtefatto(InternoAcquistoArtefatto comandoAcquistoArtefatto) {
		GruppoGiocatore gruppo = gruppo();
		Artefatto artefatto = (Artefatto) comandoAcquistoArtefatto.getOggettoDaSpostare();
		if (gruppo.compra(artefatto, prezzoAcquisto(gruppo, artefatto.getCostoAcquisto()))) {
			comandoAcquistoArtefatto.getParteRemota().removeArtefatto(artefatto);
			BusEventi.pubblica(comandoAcquistoArtefatto.approvazione());
		} else {
			BusEventi.pubblica(comandoAcquistoArtefatto.rifiuto());
		}
	}

	private static void suEventoRichiestaVenditaArtefatto(InternoVenditaArtefatto eventoRichiestaVendita) {
		vende(gruppo(), eventoRichiestaVendita);
	}

	/**
	 * Vende un artefatto del gruppo al commerciante, che lo compra solo se tratta quel genere di oggetti:
	 * l'armaiolo niente pergamene, il venditore di pergamene solo quelle.
	 * Pubblica NotificaApprovazioneVenditaArtefatto o NotificaRifiutoVenditaArtefatto.
	 *
	 * @return true se la vendita è riuscita
	 */
	static boolean vende(GruppoGiocatore gruppo, InternoVenditaArtefatto eventoRichiestaVendita) {
		Artefatto artefatto = (Artefatto) eventoRichiestaVendita.getOggettoDaSpostare();
		if (!eventoRichiestaVendita.getParteRemota().tratta(artefatto)) {
			BusEventi.pubblica(eventoRichiestaVendita.rifiuto());
			return false;
		}
		int ricavo = prezzoVendita(gruppo, artefatto.getCostoAcquisto());
		gruppo.removeArtefatto(artefatto);
		gruppo.addMonete(ricavo);
		eventoRichiestaVendita.getParteRemota().addArtefatto(artefatto);
		BusEventi.pubblica(eventoRichiestaVendita.approvazione());
		return true;
	}

	// ---- La fusione

	private static void suEventoIncantatura(InternoIncantatura comandoIncantatura) {
		incanta(gruppo(), comandoIncantatura.getBanco(), comandoIncantatura.getNomeProprio());
	}

	/**
	 * La fusione dall'incantatore: ricontrolla le regole (RegoleIncantatura), fa pagare, copia sull'artefatto
	 * gli effetti delle pergamene, che spariscono, e rimette l'artefatto nell'inventario del gruppo.
	 * Pubblica NotificaApprovazioneIncantatura o NotificaRifiutoIncantatura. Con il libro che riprogramma sul banco
	 * l'incantatore sbaglia sempre: l'artefatto perde gli incantamenti, che restano in ingredienti del Core Dump, e
	 * pubblica NotificaAvvisoIncantatura con la sua esclamazione.
	 *
	 * @return il motivo del rifiuto, oppure vuoto se la fusione è riuscita
	 */
	public static Optional<TipoMotivoRifiutoIncantatura> incanta(GruppoGiocatore gruppo, BancoDiLavoro banco, String nomeProprio) {
		Optional<TipoMotivoRifiutoIncantatura> motivo = RegoleIncantatura.verifica(banco.getInventario(), gruppo.getMonete(),
				costoFusione(gruppo, banco.getInventario()), Statistiche.getLivello());
		if (motivo.isPresent()) {
			BusEventi.pubblica(new NotificaRifiutoIncantatura(motivo.get()));
			return motivo;
		}
		int costo = costoFusione(gruppo, banco.getInventario());
		if (RegoleIncantatura.conRiprogrammazione(banco.getInventario())) {
			// Va sempre male: l'artefatto si spezza negli ingredienti dei suoi incantamenti, e tutto torna nel gruppo
			List<Artefatto> restituiti = RegoleIncantatura.riprogramma(banco);
			gruppo.subMonete(costo);
			restituiti.forEach(gruppo::addArtefatto);
			BusEventi.pubblica(new NotificaAvvisoIncantatura(ProduttoreDiArtefattiCasuale.erroreDiRiprogrammazione()));
			return Optional.empty();
		}
		Artefatto incantato = RegoleIncantatura.fondi(banco, nomeProprio);
		gruppo.subMonete(costo);
		gruppo.addArtefatto(incantato);
		BusEventi.pubblica(new NotificaApprovazioneIncantatura(incantato, costo));
		return Optional.empty();
	}

	// ---- L'alchimista

	private static void suEventoRichiestaAcquistoConsumabile(ComandoAcquistoConsumabile comandoAcquistoConsumabile) {
		GruppoGiocatore gruppo = gruppo();
		// Il prezzo lo decide l'offerta dell'alchimista; se il comando non ne corrisponde a nessuna, si ignora
		Optional<OffertaConsumabile> offerta = OfferteAlchimista.trova(gruppo, comandoAcquistoConsumabile.getTipoConsumabile(),
				comandoAcquistoConsumabile.getClasseIncantesimo(), comandoAcquistoConsumabile.getUuidPersonaggio());
		if (!offerta.isPresent()) {
			return;
		}
		int costoOggetto = prezzoAcquisto(gruppo, offerta.get().getCosto());
		if (gruppo.getMonete() >= costoOggetto) {
			switch (comandoAcquistoConsumabile.getTipoConsumabile()) {
				case POZIONE_SALUTE:
					gruppo.addPozioniSalute(1);
					break;
				case POZIONE_SALUTE_GRANDE:
					gruppo.addPozioniSaluteGrande(1);
					break;
				case POZIONE_MAGIA:
					gruppo.addPozioniMagia(1);
					break;
				case POZIONE_MAGIA_GRANDE:
					gruppo.addPozioniMagiaGrande(1);
					break;
				case AUMENTO_MAGIA_SINGOLO:
					gruppo.getPersonaggi().stream().filter(p -> p.getUuid().equals(comandoAcquistoConsumabile.getUuidPersonaggio())).findFirst()
							.ifPresent(p -> p.addMagiaMassima(CostantiPersonaggi.AUMENTO_MAGIA_DA_POZIONE_MAGIA_GRANDE, "ALCHIMISTA"));
					break;
				case AUMENTO_MAGIA_GRUPPO:
					for (Personaggio personaggio : gruppo.getPersonaggiVivi()) {
						if (!personaggio.isPNG()) {
							personaggio.addMagiaMassima(CostantiPersonaggi.AUMENTO_MAGIA_DA_POZIONE_MAGIA_GRANDE, "ALCHIMISTA");
						}
					}
					break;
				case INCANTESIMO:
					gruppo.addIncantesimi(comandoAcquistoConsumabile.getClasseIncantesimo(), 1);
					break;
				case MAPPA_PARZIALE_FORESTA:
					int x = Foresta.getPosizioneDelGruppo().getX();
					int y = Foresta.getPosizioneDelGruppo().getY();
					Foresta.ottieniMappaZona(x - 7, y - 7, x + 7, y + 7);
					break;
				case MAPPA_COMPLETA_FORESTA:
					Foresta.ottieniMappa();
					break;
				default:
					throw new IllegalArgumentException("Tipo consumabile non valido");
			}
			gruppo.subMonete(costoOggetto);
			BusEventi.pubblica(new NotificaApprovazioneAcquistoConsumabile(comandoAcquistoConsumabile, costoOggetto));
		} else {
			BusEventi.pubblica(new NotificaRifiutoAcquistoConsumabile(comandoAcquistoConsumabile));
		}
	}
}
