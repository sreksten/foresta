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
 * classe e si scrive il risultato, pubblicando le variazioni se c'è un personaggio a cui riferirle. Funzioni statiche, perché servono
 * anche alla rilettura di una partita: lì i dati sono un {@link PersonaggioMD} appena letto, i moltiplicatori vengono da un personaggio
 * modello della stessa classe e non c'è nessuno a cui riferire le variazioni (vedi PersonaggioBase.ricalcolaAttributiSecondari).
 * <p>
 * Nei metodi, {@code moltiplicatori} è il personaggio da cui si leggono i moltiplicatori della classe e {@code sorgenteEvento} quello
 * nominato nelle notifiche (null per non pubblicarne).
 */
final class AttributiSecondari {

	private AttributiSecondari() {
	}

	/**
	 * Ricalcola e scrive nel modello dati tutti gli attributi secondari. Pubblica una NotificaVariazioneStatistichePersonaggio per ogni
	 * valore cambiato, ma solo se c'è {@code sorgenteEvento} e il valore c'era già (la prima volta non è una variazione).
	 */
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
	 * Il critico (0-100): dai primari DESTREZZA e FORTUNA, con i loro modificatori, e dal moltiplicatore della classe. La formula, con i
	 * rendimenti decrescenti, sta in {@link CalcolatoreAttributiSecondari#critico}; i modificatori del CRITICO stesso li applica chi lo legge.
	 */
	static int calcolaCritico(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.critico(valoreDi(md, TipoAttributo.DESTREZZA), valoreDi(md, TipoAttributo.FORTUNA), moltiplicatori.getMoltiplicatoreCritico());
	}

	static int calcolaPrecisione(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.precisione(valoreDi(md, TipoAttributo.DESTREZZA), valoreDi(md, TipoAttributo.INTELLIGENZA), moltiplicatori.getMoltiplicatorePrecisione());
	}

	static int calcolaVelocita(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.velocita(valoreDi(md, TipoAttributo.DESTREZZA), valoreDi(md, TipoAttributo.FORTUNA), moltiplicatori.getMoltiplicatoreVelocita());
	}

	static int calcolaFurtivita(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.furtivita(valoreDi(md, TipoAttributo.DESTREZZA), valoreDi(md, TipoAttributo.FORTUNA), moltiplicatori.getMoltiplicatoreFurtivita());
	}

	static int calcolaParata(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.parata(valoreDi(md, TipoAttributo.FORZA), valoreDi(md, TipoAttributo.DESTREZZA), moltiplicatori.getMoltiplicatoreParata());
	}

	static int calcolaResistenzaMagica(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.resistenzaMagica(valoreDi(md, TipoAttributo.SAGGEZZA), valoreDi(md, TipoAttributo.INTELLIGENZA), moltiplicatori.getMoltiplicatoreResistenzaMagica());
	}

	static int calcolaPercezione(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.percezione(valoreDi(md, TipoAttributo.SAGGEZZA), valoreDi(md, TipoAttributo.DESTREZZA), moltiplicatori.getMoltiplicatorePercezione());
	}

	static int calcolaSoggezione(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.soggezione(valoreDi(md, TipoAttributo.CARISMA), valoreDi(md, TipoAttributo.FORZA), moltiplicatori.getMoltiplicatoreSoggezione());
	}

	static int calcolaFuria(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.furia(valoreDi(md, TipoAttributo.FORZA), valoreDi(md, TipoAttributo.FORTUNA), moltiplicatori.getMoltiplicatoreFuria());
	}

	static int calcolaCoraggio(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.coraggio(valoreDi(md, TipoAttributo.CARISMA), valoreDi(md, TipoAttributo.COSTITUZIONE), moltiplicatori.getMoltiplicatoreCoraggio());
	}

	static int calcolaContrattazione(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.contrattazione(valoreDi(md, TipoAttributo.CARISMA), valoreDi(md, TipoAttributo.FORTUNA), moltiplicatori.getMoltiplicatoreContrattazione());
	}

	static int calcolaValore(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.valore(valoreDi(md, TipoAttributo.SAGGEZZA), valoreDi(md, TipoAttributo.COSTITUZIONE), moltiplicatori.getMoltiplicatoreValore());
	}

	/**
	 * Quanti bersagli il personaggio minaccia insieme. I due primari da cui dipende cambiano con la classe (vedi i rami dello switch).
	 */
	static int calcolaNumeroBersagli(PersonaggioMD md, Personaggio moltiplicatori) {
		// Recuperiamo la classe per capire qual è la forza trainante del personaggio
		TipoPersonaggio classe = moltiplicatori.getClasse();

		int statPrincipale;
		int statSecondaria;

		// I due primari secondo il genere di classe. Si guarda il ruolo (TipoPersonaggio.getRuolo): la Maga va con il Mago, e così via
		switch (classe.getRuolo()) {
			case MAGO:
			case SACERDOTE:
			case DRUIDO:
			case LICH:
			case STREGA:
			case NECROMANTE:
				// I maghi concatenano minacce con la mente: Intelligenza (70%) + Saggezza (30%)
				statPrincipale = valoreDi(md, TipoAttributo.INTELLIGENZA);
				statSecondaria = valoreDi(md, TipoAttributo.SAGGEZZA);
				break;

			case BARDO:
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
				statPrincipale = valoreDi(md, TipoAttributo.CARISMA);
				statSecondaria = valoreDi(md, TipoAttributo.INTELLIGENZA);
				break;

			case LADRO:
			case ELFO:
				// Classi agili (il Ladro e l'Elfo, che combattono a due mani): Destrezza (70%) + Forza (30%)
				statPrincipale = valoreDi(md, TipoAttributo.DESTREZZA);
				statSecondaria = valoreDi(md, TipoAttributo.FORZA);
				break;

			case GUERRIERO:
			case CHIERICO:
			case CAPITANO_DELLE_GUARDIE:
			case VICHINGO:
			case MINOTAURO:
			case MINOTAURO_GIGANTE:
			case GIGANTE:
			case TITANO:
			case DRAGO:
			case ARMAIOLO:
				// I bruti e i tank fisici: Forza (70%) + Costituzione (30%)
				statPrincipale = valoreDi(md, TipoAttributo.FORZA);
				statSecondaria = valoreDi(md, TipoAttributo.COSTITUZIONE);
				break;

			default:
				// Tutti gli altri (i mostri non elencati, l'Eremita, l'Ombrafiamma): Forza (70%) + Intelligenza (30%)
				statPrincipale = valoreDi(md, TipoAttributo.FORZA);
				statSecondaria = valoreDi(md, TipoAttributo.INTELLIGENZA);
				break;
		}

		// La formula (70% la forza trainante, 30% quella di appoggio, minimo 1 bersaglio) sta nel calcolatore degli attributi
		return CalcolatoreAttributiSecondari.numeroBersagli(statPrincipale, statSecondaria, moltiplicatori.getMoltiplicatoreNumeroBersagli());
	}

	/**
	 * Il carico massimo: il potenziale che viene da FORZA, COSTITUZIONE e moltiplicatore della classe (vedi {@link #potenzialeDiCarico}),
	 * cambiato dai modificatori di CARICO_MASSIMO (quelli degli artefatti e quelli a tempo).
	 */
	static double calcolaCaricoMassimo(PersonaggioMD md, Personaggio moltiplicatori) {
		return getQuantitaModificata(md, potenzialeDiCarico(md, moltiplicatori), TipoAttributo.CARICO_MASSIMO);
	}

	/**
	 * Il carico massimo prima dei modificatori di CARICO_MASSIMO.
	 */
	static double potenzialeDiCarico(PersonaggioMD md, Personaggio moltiplicatori) {
		return CalcolatoreAttributiSecondari.potenzialeDiCarico(valoreDi(md, TipoAttributo.FORZA), valoreDi(md, TipoAttributo.COSTITUZIONE), moltiplicatori.getMoltiplicatoreCarico());
	}

	/**
	 * Riporta l'attributo del personaggio modificato sia dai suoi modificatori (anche quelli a tempo) sia da quelli degli artefatti che
	 * porta. Il valore di base, che nel modello dati è un double, si tronca a intero prima di applicare i modificatori, e si tronca di
	 * nuovo il risultato. L'attributo deve esserci nel modello dati, altrimenti PersonaggioMD.get lancia un'eccezione.
	 */
	static int valoreDi(PersonaggioMD md, TipoAttributo tipoAttributo) {
		return (int) getQuantitaModificata(md, (int) md.get(tipoAttributo), tipoAttributo);
	}

	/**
	 * Come {@link #quantitaModificata}, con anche i modificatori a tempo.
	 */
	static double getQuantitaModificata(PersonaggioMD md, double quantitaOriginale, TipoAttributo tipoAttributo) {
		return quantitaModificata(md, quantitaOriginale, tipoAttributo, true);
	}

	/**
	 * Applica a {@code quantitaOriginale} i modificatori di quell'attributo: quelli del personaggio (senza quelli a tempo se
	 * {@code conTemporanei} è false) e quelli degli artefatti equipaggiati che valgono per chi li porta. Le regole, nell'ordine:
	 * <ol>
	 * <li>se c'è un modificatore a QUANTITA_ASSOLUTA, il valore è quello e basta (se sono più d'uno, il più basso);</li>
	 * <li>altrimenti si sommano all'originale le VARIAZIONE_FISSA;</li>
	 * <li>poi si moltiplica per (1 + somma delle VARIAZIONE_PERCENTUALE / 100): +10 e -30 fanno -20%, cioè ×0,8.</li>
	 * </ol>
	 * Il confronto fra il valore con e senza i modificatori a tempo dice quanto valgono questi (vedi ModificatoriTemporanei).
	 */
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
