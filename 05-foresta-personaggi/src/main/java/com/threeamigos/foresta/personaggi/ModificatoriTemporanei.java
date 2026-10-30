package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.*;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.modificatori.ModificatoreAttivo;
import com.threeamigos.foresta.modificatori.TestoDeiModificatori;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.UnitaDurataModificatore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * I modificatori di attributo di un personaggio (benedizioni, pozioni, effetti delle armi...): li aggiunge, li fa scorrere a turni e a ore, li
 * toglie, e quando cambiano ricalcola quello che ne dipende (salute e magia massime, attributi secondari). Lo tiene {@link PersonaggioBase},
 * che ne inoltra i metodi di {@link Personaggio}; i dati stanno nel {@link PersonaggioMD} del personaggio, che qui si rilegge a ogni uso
 * perché può essere sostituito (vedi {@link PersonaggioBase#setModelloDati}).
 */
final class ModificatoriTemporanei {

	private final PersonaggioBase p;

	ModificatoriTemporanei(PersonaggioBase personaggio) {
		this.p = personaggio;
	}

	private PersonaggioMD md() {
		return p.getModelloDati();
	}

	public void addModificatore(ModificatoreAttributoMD modificatore) {
		if (modificatore.getBersaglio() != BersaglioModificatore.PORTATORE) {
			throw new IllegalArgumentException("Un modificatore sul bersaglio va messo sul bersaglio con suPortatore()");
		}
		if (modificatore.isTemporaneo()) {
			Optional<ModificatoreAttributoMD> uguale = md().getModificatori().stream()
					.filter(m -> m.stessoEffetto(modificatore)).findFirst();
			if (uguale.isPresent()) {
				// Lo stesso modificatore non si somma: rinfresca la durata, se quella nuova è più lunga
				ModificatoreAttributoMD presente = uguale.get();
				if (modificatore.getDurata() > presente.getDurata()) {
					sostituisciModificatore(presente, modificatore);
					pubblicaVariazioneModificatore(NotificaVariazioneModificatorePersonaggio.TipoVariazione.MODIFICA,
							modificatore, presente.getDurata(), modificatore.getDurata());
				}
				return;
			}
		}
		modificaModificatori(() -> md().aggiungiModificatore(modificatore));
		pubblicaVariazioneModificatore(NotificaVariazioneModificatorePersonaggio.TipoVariazione.AGGIUNTA,
				modificatore, 0, modificatore.getDurata());
	}

	public Collection<ModificatoreAttributoMD> getModificatoriTemporanei() {
		return md().getModificatori().stream().filter(ModificatoreAttributoMD::isTemporaneo).collect(Collectors.toList());
	}

	public void riduciModificatori(UnitaDurataModificatore unitaDurata) {
		for (ModificatoreAttributoMD modificatore : new ArrayList<>(md().getModificatori())) {
			if (modificatore.getUnitaDurata() != unitaDurata) {
				continue;
			}
			int durataPrecedente = modificatore.getDurata();
			if (durataPrecedente > 1) {
				ModificatoreAttributoMD ridotto = modificatore.conDurata(durataPrecedente - 1);
				sostituisciModificatore(modificatore, ridotto);
				pubblicaVariazioneModificatore(NotificaVariazioneModificatorePersonaggio.TipoVariazione.MODIFICA,
						ridotto, durataPrecedente, durataPrecedente - 1);
			} else {
				rimuoviModificatore(modificatore);
			}
		}
	}

	public void rimuoviIModificatoriTemporaneiRimovibili() {
		for (ModificatoreAttributoMD modificatore : getModificatoriTemporanei()) {
			if (modificatore.isRimovibile()) {
				rimuoviModificatore(modificatore);
			}
		}
	}

	private void rimuoviModificatore(ModificatoreAttributoMD modificatore) {
		modificaModificatori(() -> md().togliModificatore(modificatore));
		pubblicaVariazioneModificatore(NotificaVariazioneModificatorePersonaggio.TipoVariazione.RIMOZIONE,
				modificatore, modificatore.getDurata(), 0);
	}

	private void sostituisciModificatore(ModificatoreAttributoMD vecchio, ModificatoreAttributoMD nuovo) {
		modificaModificatori(() -> {
			md().togliModificatore(vecchio);
			md().aggiungiModificatore(nuovo);
		});
	}

	/**
	 * Cambia i modificatori e ricalcola quello che ne dipende: salute e magia massime (con le loro notifiche, e la
	 * salute e la magia attuali non restano sopra i nuovi massimi) e gli attributi secondari.
	 */
	void modificaModificatori(Runnable modifica) {
		int saluteMassimaPrecedente = p.getSaluteMassima();
		int magiaMassimaPrecedente = p.getMagiaMassima();

		modifica.run();

		int saluteMassimaRicalcolata = p.getSaluteMassima();
		if (saluteMassimaPrecedente != saluteMassimaRicalcolata) {
			if ((int) md().get(TipoAttributo.SALUTE) > saluteMassimaRicalcolata) {
				md().setSalute(saluteMassimaRicalcolata);
			}
			BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.SALUTE_MASSIMA, saluteMassimaPrecedente, saluteMassimaRicalcolata));
		}

		int magiaMassimaRicalcolata = p.getMagiaMassima();
		if (magiaMassimaPrecedente != magiaMassimaRicalcolata) {
			if ((int) md().get(TipoAttributo.MAGIA) > magiaMassimaRicalcolata) {
				md().setMagia(magiaMassimaRicalcolata);
			}
			BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(p, TipoAttributo.MAGIA_MASSIMA, magiaMassimaPrecedente, magiaMassimaRicalcolata));
		}

		p.ricalcolaAttributiSecondari();
	}

	private void pubblicaVariazioneModificatore(NotificaVariazioneModificatorePersonaggio.TipoVariazione tipo,
												ModificatoreAttributoMD modificatore, int durataPrecedente, int nuovaDurata) {
		BusEventi.pubblica(new NotificaVariazioneModificatorePersonaggio(p, tipo, modificatore, durataPrecedente, nuovaDurata));
		// Solo i modificatori a tempo si raccontano nel testo: i permanenti (benedizioni, lealtà) hanno già il loro
		// messaggio, e il semplice scorrere del tempo (MODIFICA con la durata che scende) non va scritto ogni turno
		if (!modificatore.isTemporaneo()) {
			return;
		}
		String nome = p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE, Personaggio.OpzioniGetNome.INIZIALE_MAIUSCOLA);
		String effetto = TestoDeiModificatori.effetto(modificatore);
		switch (tipo) {
			case AGGIUNTA:
				BusEventi.pubblica(new NotificaTestoFrase(nome + ": " + effetto + " " + TestoDeiModificatori.durata(modificatore) + "."));
				break;
			case MODIFICA:
				if (nuovaDurata > durataPrecedente) {
					BusEventi.pubblica(new NotificaTestoFrase(nome + ": " + effetto + " " + TestoDeiModificatori.durata(modificatore) + "."));
				}
				break;
			case RIMOZIONE:
				// "Per il guerriero" ma "Per Aldo": l'iniziale minuscola è solo dell'articolo, un nome proprio resta com'è
				BusEventi.pubblica(new NotificaTestoFrase("Per "
						+ p.getNome(Personaggio.OpzioniGetNome.INCLUDI_ARTICOLO_DETERMINATIVO_SINGOLARE)
						+ " non vale più: " + effetto + "."));
				break;
			default:
				break;
		}
	}

	public double getVariazioneDaTemporanei(TipoAttributo tipoAttributo) {
		double base = p.baseDelModificabile(tipoAttributo);
		return AttributiSecondari.quantitaModificata(md(), base, tipoAttributo, true) - AttributiSecondari.quantitaModificata(md(), base, tipoAttributo, false);
	}

	public Optional<ModificatoreAttivo> getFissaggio(TipoAttributo tipoAttributo) {
		ModificatoreAttivo fissaggio = null;
		for (ModificatoreAttributoMD m : md().getModificatori()) {
			if (m.getTipoAttributo() == tipoAttributo && m.getTipoModificatoreAttributo() == TipoModificatore.QUANTITA_ASSOLUTA
					&& (fissaggio == null || m.getQuantita() < fissaggio.getModificatore().getQuantita())) {
				fissaggio = new ModificatoreAttivo(m, m.getNote());
			}
		}
		for (ArtefattoMD artefatto : md().getArtefatti()) {
			for (ModificatoreAttributoMD m : artefatto.getModificatori()) {
				if (m.getBersaglio() == BersaglioModificatore.PORTATORE && m.getTipoAttributo() == tipoAttributo
						&& m.getTipoModificatoreAttributo() == TipoModificatore.QUANTITA_ASSOLUTA
						&& (fissaggio == null || m.getQuantita() < fissaggio.getModificatore().getQuantita())) {
					fissaggio = new ModificatoreAttivo(m, artefatto.getNome());
				}
			}
		}
		return Optional.ofNullable(fissaggio);
	}
}
