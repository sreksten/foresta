package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.TatticaMD;
import com.threeamigos.foresta.modellodati.TatticheMD;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.function.Supplier;

/**
 * Le tattiche dei personaggi della partita in corso. Il personaggio non sa di averne una: la tattica sta qui, legata al personaggio
 * dal suo identificativo. Dove sono i dati lo dice chi li custodisce (vedi {@link #setFornitoreDiDati}).
 *
 * @author Stefano Reksten
 */
public final class TatticheDeiPersonaggi {

	// Da dove prendere i dati della partita in corso: le tattiche non sanno chi li custodisce
	private static volatile Supplier<TatticheMD> fornitoreDiDati;

	private TatticheDeiPersonaggi() {
	}

	/**
	 * Dice da dove prendere le tattiche della partita in corso (lo fa chi custodisce i dati: ogni volta si prendono da quelli in corso).
	 */
	public static void setFornitoreDiDati(Supplier<TatticheMD> fornitore) {
		fornitoreDiDati = fornitore;
	}

	private static TatticheMD dati() {
		Supplier<TatticheMD> fornitore = fornitoreDiDati;
		if (fornitore == null) {
			throw new IllegalStateException("Nessuno ha detto alle tattiche dove sono i loro dati (vedi setFornitoreDiDati)");
		}
		return fornitore.get();
	}

	/**
	 * La tattica del personaggio (tattiche_di_combattimento_automatiche.md §11), o null se non ne ha una (oggi i mostri, che scelgono
	 * con la tattica base della loro classe, vedi {@link RegistroTattiche}).
	 */
	public static TatticaMD di(Personaggio personaggio) {
		return dati().get(personaggio.getModelloDati().getUuid());
	}

	/**
	 * Dà una tattica al personaggio; null gliela toglie.
	 */
	public static void imposta(Personaggio personaggio, TatticaMD tattica) {
		dati().imposta(personaggio.getModelloDati().getUuid(), tattica);
	}
}
