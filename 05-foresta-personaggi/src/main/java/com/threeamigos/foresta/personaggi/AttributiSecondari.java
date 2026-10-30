package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.artefatti.leggendari.RegoleSetLeggendari;
import com.threeamigos.foresta.attributi.CalcolatoreAttributiSecondari;
import com.threeamigos.foresta.eventi.BusEventi;
import com.threeamigos.foresta.eventi.notifiche.NotificaVariazioneStatistichePersonaggio;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.modellodati.EffettoDiStatoMD;
import com.threeamigos.foresta.modellodati.ModificatoreAttributoMD;
import com.threeamigos.foresta.modellodati.PersonaggioMD;
import com.threeamigos.foresta.tipi.BersaglioModificatore;
import com.threeamigos.foresta.tipi.TipoAttributo;
import com.threeamigos.foresta.tipi.TipoEffettoDiStato;
import com.threeamigos.foresta.tipi.TipoModificatore;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Gli attributi secondari di un personaggio (carico massimo, critico, precisione, velocità, furtività, parata, resistenza magica, percezione,
 * soggezione, furia, coraggio, valore, contrattazione, numero di bersagli) e il valore di ogni attributo con i suoi modificatori. Le formule
 * stanno in {@link CalcolatoreAttributiSecondari}; qui si leggono i primari dal {@link PersonaggioMD}, si sceglie il moltiplicatore della
 * classe e si scrive il risultato, pubblicando le variazioni se c'è un personaggio a cui riferirle. Funzioni statiche: servono anche in
 * fase di caricamento, quando il personaggio non esiste ancora.
 */
final class AttributiSecondari {

	private AttributiSecondari() {
	}

	static void ricalcola(PersonaggioMD md, Personaggio moltiplicatori, Personaggio sorgenteEvento) {
		// Il carico massimo è un massimo, non un valore: si legge e si scrive con getMassimo/setMassimo
		Optional<Double> valorePrecedente = md.getMassimo(TipoAttributo.CARICO_MASSIMO);
		double valoreAttuale = calcolaCaricoMassimo(md, moltiplicatori);
		md.setMassimo(TipoAttributo.CARICO_MASSIMO, valoreAttuale);
		if (sorgenteEvento != null && valorePrecedente.isPresent() && valorePrecedente.get() != valoreAttuale) {
			BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(sorgenteEvento, TipoAttributo.CARICO_MASSIMO, valorePrecedente.get(), valoreAttuale));
		}
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.CRITICO, calcolaCritico(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.PRECISIONE, calcolaPrecisione(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.VELOCITA, calcolaVelocita(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.FURTIVITA, calcolaFurtivita(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.PARATA, calcolaParata(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.RESISTENZA_MAGICA, calcolaResistenzaMagica(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.PERCEZIONE, calcolaPercezione(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.SOGGEZIONE, calcolaSoggezione(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.FURIA, calcolaFuria(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.CORAGGIO, calcolaCoraggio(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.VALORE, calcolaValore(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.CONTRATTAZIONE, calcolaContrattazione(md, moltiplicatori));
		ricalcolaAttributoSecondario(md, sorgenteEvento, TipoAttributo.NUMERO_BERSAGLI, calcolaNumeroBersagli(md, moltiplicatori));
	}

	static void ricalcolaAttributoSecondario(PersonaggioMD md, Personaggio sorgenteEvento, TipoAttributo tipoAttributo, double valoreRicalcolato) {
		Optional<Double> valorePrecedente = md.getOptional(tipoAttributo);
		md.set(tipoAttributo, valoreRicalcolato);
		if (sorgenteEvento != null && valorePrecedente.isPresent() && valorePrecedente.get() != valoreRicalcolato) {
			BusEventi.pubblica(new NotificaVariazioneStatistichePersonaggio(sorgenteEvento, tipoAttributo, valorePrecedente.get(), valoreRicalcolato));
		}
	}

	/**
	 * Calcola il critico (0-100) basandosi UNICAMENTE sulle statistiche primarie
	 * e sul moltiplicatore della classe, mantenendo i rendimenti decrescenti.
	 */
	static int calcolaCritico(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.critico(get(md, TipoAttributo.DESTREZZA), get(md, TipoAttributo.FORTUNA), moltiplicatori.getMoltiplicatoreCritico());
	}

	static int calcolaPrecisione(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.precisione(get(md, TipoAttributo.DESTREZZA), get(md, TipoAttributo.INTELLIGENZA), moltiplicatori.getMoltiplicatorePrecisione());
	}

	static int calcolaVelocita(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.velocita(get(md, TipoAttributo.DESTREZZA), get(md, TipoAttributo.FORTUNA), moltiplicatori.getMoltiplicatoreVelocita());
	}

	static int calcolaFurtivita(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.furtivita(get(md, TipoAttributo.DESTREZZA), get(md, TipoAttributo.FORTUNA), moltiplicatori.getMoltiplicatoreFurtivita());
	}

	static int calcolaParata(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.parata(get(md, TipoAttributo.FORZA), get(md, TipoAttributo.DESTREZZA), moltiplicatori.getMoltiplicatoreParata());
	}

	static int calcolaResistenzaMagica(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.resistenzaMagica(get(md, TipoAttributo.SAGGEZZA), get(md, TipoAttributo.INTELLIGENZA), moltiplicatori.getMoltiplicatoreResistenzaMagica());
	}

	static int calcolaPercezione(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.percezione(get(md, TipoAttributo.SAGGEZZA), get(md, TipoAttributo.DESTREZZA), moltiplicatori.getMoltiplicatorePercezione());
	}

	static int calcolaSoggezione(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.soggezione(get(md, TipoAttributo.CARISMA), get(md, TipoAttributo.FORZA), moltiplicatori.getMoltiplicatoreSoggezione());
	}

	static int calcolaFuria(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.furia(get(md, TipoAttributo.FORZA), get(md, TipoAttributo.FORTUNA), moltiplicatori.getMoltiplicatoreFuria());
	}

	static int calcolaCoraggio(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.coraggio(get(md, TipoAttributo.CARISMA), get(md, TipoAttributo.COSTITUZIONE), moltiplicatori.getMoltiplicatoreCoraggio());
	}

	static int calcolaContrattazione(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.contrattazione(get(md, TipoAttributo.CARISMA), get(md, TipoAttributo.FORTUNA), moltiplicatori.getMoltiplicatoreContrattazione());
	}

	static int calcolaValore(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.valore(get(md, TipoAttributo.SAGGEZZA), get(md, TipoAttributo.COSTITUZIONE), moltiplicatori.getMoltiplicatoreValore());
	}

	static int calcolaNumeroBersagli(PersonaggioMD md, Personaggio moltiplicatori) {
		// Recuperiamo la classe per capire qual è la forza trainante del personaggio
		TipoPersonaggio classe = moltiplicatori.getClasse();

		int statPrincipale;
		int statSecondaria;

		// BIVIO DI BILANCIAMENTO IN BASE ALL'ARCHETIPO DI CLASSE
		switch (classe) {
			case MAGO:
			case MAGA:
			case SACERDOTE:
			case SACERDOTESSA:
			case DRUIDO:
			case DRUIDA:
			case LICH:
			case STREGA:
			case NECROMANTE:
				// I maghi concatenano minacce con la mente: Intelligenza (70%) + Saggezza (30%)
				statPrincipale = get(md, TipoAttributo.INTELLIGENZA);
				statSecondaria = get(md, TipoAttributo.SAGGEZZA);
				break;

			case BARDO:
			case CANTASTORIE:
			case VIANDANTE:
			case BARDO_DELLE_LOCANDE:
			case LOCANDIERE:
			case ALCHIMISTA:
			case VENDITORE_DI_PERGAMENE:
			case INCANTATORE:
			case MOGLIE_DEL_BARDO:
			case UOMO:
			case BORGOMASTRO:
			case DONNA:
				// I bardi ammaliano o spaventano folle intere: Carisma (70%) + Intelligenza (30%)
				statPrincipale = get(md, TipoAttributo.CARISMA);
				statSecondaria = get(md, TipoAttributo.INTELLIGENZA);
				break;

			case LADRO:
			case LADRA:
			case ELFO:
			case ELFA:
				// Classi agili (e il Ladro che mena a due mani): Destrezza (70%) + Fortuna/Forza (30%)
				statPrincipale = get(md, TipoAttributo.DESTREZZA);
				statSecondaria = get(md, TipoAttributo.FORZA);
				break;

			case GUERRIERO:
			case GUERRIERA:
			case CHIERICO:
			case CHIERICA:
			case CAPITANO_DELLE_GUARDIE:
			case MINOTAURO:
			case MINOTAURO_GIGANTE:
			case GIGANTE:
			case TITANO:
			case DRAGO:
			case ARMAIOLO:
				// I bruti e i tank fisici: Forza (70%) + Costituzione (30%)
				statPrincipale = get(md, TipoAttributo.FORZA);
				statSecondaria = get(md, TipoAttributo.COSTITUZIONE);
				break;

			default:
				// Fail-safe per mostri generici ed eremiti (Forza ed equilibrio mentale)
				statPrincipale = get(md, TipoAttributo.FORZA);
				statSecondaria = get(md, TipoAttributo.INTELLIGENZA);
				break;
		}

		// La formula (70% la forza trainante, 30% quella di appoggio, minimo 1 bersaglio) sta nel calcolatore degli attributi
		return CalcolatoreAttributiSecondari.numeroBersagli(statPrincipale, statSecondaria, moltiplicatori.getMoltiplicatoreNumeroBersagli());
	}

	/**
	 * Calcola il carico massimo basandosi UNICAMENTE sulle statistiche primarie
	 * e sul moltiplicatore della classe, mantenendo i rendimenti decrescenti.
	 */
	static double calcolaCaricoMassimo(PersonaggioMD md, Personaggio moltiplicatori) {
		return getQuantitaModificata(md, potenzialeDiCarico(md, moltiplicatori), TipoAttributo.CARICO_MASSIMO);
	}

	/**
	 * Il carico massimo prima dei modificatori di CARICO_MASSIMO.
	 */
	static double potenzialeDiCarico(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.potenzialeDiCarico(get(md, TipoAttributo.FORZA), get(md, TipoAttributo.COSTITUZIONE), moltiplicatori.getMoltiplicatoreCarico());
	}

	/**
	 * Riporta l'attributo del personaggio modificato sia dai modificatori locali che quelli degli artefatti. Il valore di
	 * base, che nel modello dati è un double, qui si usa troncato a intero: è così che il gioco lo ha sempre calcolato, e
	 * per questo il modello dati non ha più un getter per ogni attributo.
	 */
	static int get(PersonaggioMD md, TipoAttributo tipoAttributo) {
		return (int) getQuantitaModificata(md, (int) md.get(tipoAttributo), tipoAttributo);
	}

	static double getQuantitaModificata(PersonaggioMD md, double quantitaOriginale, TipoAttributo tipoAttributo) {
		return quantitaModificata(md, quantitaOriginale, tipoAttributo, true);
	}

	static double quantitaModificata(PersonaggioMD md, double quantitaOriginale, TipoAttributo tipoAttributo,
											 boolean conTemporanei) {
		List<ModificatoreAttributoMD> modificatoriLocali = new ArrayList<>();

		md.getModificatori()
				.stream()
				.filter(m -> m.getTipoAttributo() == tipoAttributo)
				.filter(m -> conTemporanei || !m.isTemporaneo())
				.forEach(modificatoriLocali::add);

		// I bonus dei pezzi di un set leggendario completo sono moltiplicati (vedi RegoleSetLeggendari)
		for (ArtefattoMD artefatto : md.getArtefatti()) {
			double moltiplicatore = RegoleSetLeggendari.moltiplicatore(md.getArtefatti(), artefatto);
			artefatto.getModificatori().stream()
					.filter(m -> m.getTipoAttributo() == tipoAttributo)
					.filter(m -> m.getBersaglio() == BersaglioModificatore.PORTATORE)
					.map(m -> RegoleSetLeggendari.applica(m, moltiplicatore))
					.forEach(modificatoriLocali::add);
		}

		OptionalDouble modificatoreAssoluto = modificatoriLocali
				.stream()
				.filter(m -> m.getTipoModificatoreAttributo() == TipoModificatore.QUANTITA_ASSOLUTA)
				.mapToDouble(ModificatoreAttributoMD::getQuantita).min();

		if (modificatoreAssoluto.isPresent())
			return modificatoreAssoluto.getAsDouble();

		double quantitaFisse = modificatoriLocali.stream()
				.filter(m -> m.getTipoModificatoreAttributo() == TipoModificatore.VARIAZIONE_FISSA)
				.mapToDouble(ModificatoreAttributoMD::getQuantita).sum();

		quantitaOriginale += quantitaFisse;

		double quantitaPercentuali = modificatoriLocali.stream()
				.filter(m -> m.getTipoModificatoreAttributo() == TipoModificatore.VARIAZIONE_PERCENTUALE)
				.mapToDouble(ModificatoreAttributoMD::getQuantita).sum();

		// Il canto di valore: ISPIRATO dà al coraggio e alla precisione una percentuale in più, che si somma alle altre
		if (conTemporanei && (tipoAttributo == TipoAttributo.CORAGGIO || tipoAttributo == TipoAttributo.PRECISIONE)) {
			quantitaPercentuali += md.getEffettiDiStato().stream()
					.filter(e -> e.getTipoEffettoDiStato() == TipoEffettoDiStato.ISPIRATO)
					.mapToDouble(EffettoDiStatoMD::getIntensita).sum();
		}

		return quantitaOriginale * (1 + quantitaPercentuali / 100);
	}
}
