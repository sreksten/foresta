package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.motore.Statistiche;
import com.threeamigos.foresta.motore.modellodati.ArtefattoMD;
import com.threeamigos.foresta.motore.modellodati.RaritaArtefatto;
import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;
import com.threeamigos.foresta.oggetti.Artefatto;
import com.threeamigos.foresta.oggetti.GeneratoreArtefatti;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * L'equipaggiamento di base con cui un personaggio giocante entra nel gruppo, il protagonista a inizio partita e
 * i compagni reclutati: un'arma che la sua classe sa usare e una protezione leggera, comuni e senza incantamenti.
 * Il livello di ogni pezzo è quello del mondo meno un numero a caso fra 0 e 2, mai sotto 1 e mai sopra il
 * livello del personaggio (che altrimenti non potrebbe equipaggiarlo).
 */
public final class EquipaggiamentoIniziale {

	private EquipaggiamentoIniziale() {
	}

	/**
	 * I pezzi della dotazione di una classe giocante; nessuno per le altre
	 */
	static List<TipoArtefatto> perClasse(ClassePersonaggio classe) {
		switch (classe) {
			case GUERRIERO:
			case GUERRIERA:
				return Arrays.asList(TipoArtefatto.SPADA, TipoArtefatto.SCUDO);
			case LADRO:
			case LADRA:
			case ELFO:
			case ELFA:
			case BARDO:
			case CANTASTORIE:
				return Arrays.asList(TipoArtefatto.SPADA, TipoArtefatto.VESTE);
			case MAGO:
			case MAGA:
				return Arrays.asList(TipoArtefatto.BASTONE_MAGICO, TipoArtefatto.VESTE);
			default:
				return Collections.emptyList();
		}
	}

	/**
	 * Il livello del mondo meno un numero a caso fra 0 e 2, mai sotto 1: quello dei compagni reclutati e dei
	 * pezzi della dotazione
	 */
	public static int livelloCasualeDalMondo() {
		return Math.max(1, Statistiche.getLivello() - Dado.tira(0, 2));
	}

	/**
	 * Dà al personaggio la dotazione della sua classe
	 */
	public static void equipaggia(Personaggio personaggio) {
		for (TipoArtefatto tipo : perClasse(personaggio.getClasse())) {
			int livello = Math.min(personaggio.getLivello(), livelloCasualeDalMondo());
			Artefatto artefatto = GeneratoreArtefatti.istanza().generaArtefatto(tipo, livello);
			// Di base: niente rarità, incantamenti o nome proprio che il generatore dà ogni tanto
			ArtefattoMD md = artefatto.getModelloDati();
			md.setRarita(RaritaArtefatto.COMUNE);
			md.getIncantamenti().clear();
			md.setNomeProprio(null);
			if (!personaggio.puoEquipaggiare(artefatto).isPresent()) {
				personaggio.addArtefatto(artefatto);
			}
		}
	}
}
