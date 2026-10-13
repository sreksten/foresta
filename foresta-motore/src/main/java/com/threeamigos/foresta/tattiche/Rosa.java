package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.tattiche.TipoRosa;

import java.util.List;

/**
 * L'insieme da cui il selettore di una voce pesca i candidati, prima dei filtri (vedi tattiche_di_combattimento_automatiche.md §11.3).
 * Una classe per ogni {@link TipoRosa}, senza stato; si trova con {@link Rose#di(TipoRosa)}.
 */
public interface Rosa {

	TipoRosa getTipo();

	/**
	 * I candidati, nell'ordine del gruppo (nelle unioni prima i personaggi e poi gli ospiti), così "il primo" è ripetibile. Una lista nuova,
	 * non modificabile. Tranne per ALLEATI_CADUTI, sono solo personaggi in grado di essere scelti: vivi e in campo.
	 *
	 * @throws IllegalStateException se la rosa non vale per il lato dell'attore (vedi {@link TipoRosa#valePer(boolean)}): è un errore della
	 *                               tattica, che la lettura deve aver già rifiutato
	 */
	List<Personaggio> candidati(ContestoTattico contesto);

	/**
	 * Controllo comune alle rose che valgono per un lato solo
	 */
	default void verificaLato(ContestoTattico contesto) {
		if (!getTipo().valePer(contesto.isAttoreDelGiocatore())) {
			throw new IllegalStateException("La rosa " + getTipo() + " non vale per " + (contesto.isAttoreDelGiocatore() ? "i personaggi del giocatore" : "i mostri"));
		}
	}

	/**
	 * Quelli che possono essere scelti: vivi e in campo (non in panchina)
	 */
	static List<Personaggio> inCampo(List<Personaggio> personaggi) {
		List<Personaggio> inCampo = new java.util.ArrayList<>();
		for (Personaggio personaggio : personaggi) {
			if (!personaggio.isFuoriCombattimento()) {
				inCampo.add(personaggio);
			}
		}
		return inCampo;
	}
}
