package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.interni.InternoCaratteristicaOsservata;
import com.threeamigos.foresta.eventi.interni.InternoMessaggio;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneEffettoDiStatoPersonaggio;
import com.threeamigos.foresta.modellodati.EffettoDiStatoMD;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.tipi.CaratteristicaDiClasse;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Gli effetti di stato di un personaggio (veleno, sonno, berserk, rigenerazione...) e le ricariche delle sue abilità: li aggiunge e li rinforza,
 * ne applica i danni nel tempo, li fa scorrere a ogni turno e li toglie. Lo tiene {@link PersonaggioBase}, che ne inoltra i metodi di
 * {@link Personaggio}; i dati stanno nel {@link PersonaggioMD} del personaggio, che qui si rilegge a ogni uso perché può essere sostituito.
 */
final class EffettiDiStato {

	private final PersonaggioBase p;

	EffettiDiStato(PersonaggioBase personaggio) {
		this.p = personaggio;
	}

	private PersonaggioMD md() {
		return p.getModelloDati();
	}

	public Collection<EffettoDiStatoMD> getEffettiDiStato() {
		return md().getEffettiDiStato();
	}

	public void addEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato, int durata, int danniNelTempo) {
		addEffettoDiStato(tipoEffettoDiStato, durata, danniNelTempo, 0.0d);
	}

	public void addEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato, int durata, int danniNelTempo, double intensita) {
		if (!p.isVivo()) {
			return;
		}
		if (p.isImmuneAEffetto(tipoEffettoDiStato)) {
			// Il bestiario si ricorda dell'immunità, se è della classe (non quella di un solo personaggio)
			BusEventi.pubblica(new InternoCaratteristicaOsservata(p.getClasse(), CaratteristicaDiClasse.Genere.EFFETTO, tipoEffettoDiStato));
			BusEventi.pubblica(new InternoMessaggio(
					p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA) +
							" è immune a " + tipoEffettoDiStato.getDescrizione() + "."));
			return;
		}
		if (durata <= 0) {
			throw new IllegalArgumentException("Valore effetto di stato non valido");
		}
		Collection<EffettoDiStatoMD> effettiDiStato = md().getEffettiDiStato();
		Optional<EffettoDiStatoMD> equivalenteOpt = effettiDiStato
				.stream().
				filter(e -> e.getTipoEffettoDiStato() == tipoEffettoDiStato)
				.findFirst();
		if (equivalenteOpt.isPresent()) {
			EffettoDiStatoMD equivalente = equivalenteOpt.get();
			boolean duraDiPiu = equivalente.getDurata() < durata;
			boolean aumentaDanni = equivalente.getDanniNelTempo() < danniNelTempo;
			boolean aumentaIntensita = equivalente.getIntensita() < intensita;
			if (duraDiPiu || aumentaDanni || aumentaIntensita) {
				if (duraDiPiu) {
					equivalente.setDurata(durata);
				}
				if (aumentaDanni) {
					equivalente.setDanniNelTempo(danniNelTempo);
				}
				if (aumentaIntensita) {
					equivalente.setIntensita(intensita);
				}
				BusEventi.pubblica(new NotificaVariazioneEffettoDiStatoPersonaggio(p,
						NotificaVariazioneEffettoDiStatoPersonaggio.TipoVariazione.RINFORZO, tipoEffettoDiStato,
						equivalente.getDurata(), equivalente.getDanniNelTempo()));
			}
		} else {
			md().aggiungiEffettoDiStato(new EffettoDiStatoMD(tipoEffettoDiStato, durata, danniNelTempo, intensita));
			BusEventi.pubblica(new NotificaVariazioneEffettoDiStatoPersonaggio(p,
					NotificaVariazioneEffettoDiStatoPersonaggio.TipoVariazione.AGGIUNTA, tipoEffettoDiStato,
					-1, durata));
		}
	}

	public void applicaDanniDaEffettiDiStato() {
		// Copia perché p.subSalute() può a sua volta aggiungere un nuovo effetto di stato
		// (es. BERSERK) alla stessa collezione che stiamo scorrendo.
		for (EffettoDiStatoMD effettoDiStato : new ArrayList<>(getEffettiDiStato())) {
			if (effettoDiStato.getDanniNelTempo() > 0) {
				p.subSalute(effettoDiStato.getDanniNelTempo(), null,
						Personaggio.NotificaFerite.NO, Personaggio.NotificaMorte.NO);
			}
			// La rigenerazione del Druido: la quantità che guarisce a ogni turno sta nell'intensità dell'effetto
			if (effettoDiStato.getTipoEffettoDiStato() == TipoEffettoDiStato.RIGENERAZIONE && p.isVivo() && p.isCurabile()) {
				p.addSalute((int) effettoDiStato.getIntensita());
			}
		}
	}

	public int getRicarica(String nome) {
		return md().getRicarica(nome);
	}

	public Map<String, Integer> getRicariche() {
		return md().getRicariche();
	}

	public void setRicarica(String nome, int turni) {
		md().setRicarica(nome, turni);
	}

	public void riduciRicariche() {
		for (Map.Entry<String, Integer> ricarica : new ArrayList<>(md().getRicariche().entrySet())) {
			md().setRicarica(ricarica.getKey(), ricarica.getValue() - 1);
		}
	}

	public void riduciEffettiDiStato() {
		List<EffettoDiStatoMD> effettiDiStatoDaRimuovere = new ArrayList<>();
		for (EffettoDiStatoMD effettoDiStato : getEffettiDiStato()) {
			int valorePrecedente = effettoDiStato.getDurata();
			int valoreAttuale = valorePrecedente - 1;
			if (valoreAttuale > 0) {
				effettoDiStato.setDurata(valoreAttuale);
				BusEventi.pubblica(new NotificaVariazioneEffettoDiStatoPersonaggio(p,
						NotificaVariazioneEffettoDiStatoPersonaggio.TipoVariazione.DECADIMENTO,
						effettoDiStato.getTipoEffettoDiStato(),
						valorePrecedente, valoreAttuale));
			} else {
				effettiDiStatoDaRimuovere.add(effettoDiStato);
			}
		}
		effettiDiStatoDaRimuovere.forEach(e -> {
			md().togliEffettoDiStato(e);
			BusEventi.pubblica(new NotificaVariazioneEffettoDiStatoPersonaggio(p,
					NotificaVariazioneEffettoDiStatoPersonaggio.TipoVariazione.RIMOZIONE,
					e.getTipoEffettoDiStato(), 1, 0));
		});
	}

	public void rimuoviTuttiGliEffettiDiStato() {
		rimuoviEffettoDiStato(null);
	}

	public void rimuoviGliEffettiDiStatoNegativi() {
		getEffettiDiStato().stream().map(EffettoDiStatoMD::getTipoEffettoDiStato).filter(t -> !t.isBenefico()).distinct()
				.collect(Collectors.toList()).forEach(this::rimuoviEffettoDiStato);
	}

	public void rimuoviEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato) {
		List<EffettoDiStatoMD> effettiDiStatoDaRimuovere = getEffettiDiStato()
				.stream()
				.filter(e -> tipoEffettoDiStato == null || e.getTipoEffettoDiStato() == tipoEffettoDiStato)
				.collect(Collectors.toList());
		effettiDiStatoDaRimuovere.forEach(effettoDiStato -> {
			md().togliEffettoDiStato(effettoDiStato);
			BusEventi.pubblica(new NotificaVariazioneEffettoDiStatoPersonaggio(p,
					NotificaVariazioneEffettoDiStatoPersonaggio.TipoVariazione.RIMOZIONE, effettoDiStato.getTipoEffettoDiStato(),
					effettoDiStato.getDurata(), 0));
		});
	}

	public boolean hasEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato) {
		return md().getEffettiDiStato().stream().anyMatch(e -> e.getTipoEffettoDiStato() == tipoEffettoDiStato);
	}

	public int getQuantitaEffettoDiStato(TipoEffettoDiStato tipoEffettoDiStato) {
		return md().getEffettiDiStato().stream().filter(e -> e.getTipoEffettoDiStato() == tipoEffettoDiStato).mapToInt(EffettoDiStatoMD::getDurata).sum();
	}
}
