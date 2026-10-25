package com.threeamigos.foresta.personaggi;

import com.threeamigos.foresta.artefatti.Artefatto;
import com.threeamigos.foresta.modellodati.ArtefattoMD;
import com.threeamigos.foresta.artefatti.GeneratoreArtefatti;
import com.threeamigos.foresta.tipi.TipoArtefatto;
import com.threeamigos.foresta.tipi.TipoRaritaArtefatto;
import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoPersonaggio;

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
	static List<TipoArtefatto> perClasse(TipoPersonaggio classe) {
		switch (classe) {
			case GUERRIERO:
			case GUERRIERA:
			case CAPITANO_DELLE_GUARDIE:
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
			case SACERDOTE:
			case SACERDOTESSA:
				return Arrays.asList(TipoArtefatto.BASTONE_MAGICO, TipoArtefatto.VESTE);
			case CHIERICO:
			case CHIERICA:
				return Arrays.asList(TipoArtefatto.MAZZA, TipoArtefatto.SCUDO);
			case DRUIDO:
			case DRUIDA:
				return Arrays.asList(TipoArtefatto.BASTONE_MAGICO, TipoArtefatto.VESTE);
			default:
				return Collections.emptyList();
		}
	}

	/**
	 * Il livello del mondo meno un numero a caso fra 0 e 2, mai sotto 1: quello dei compagni reclutati e dei
	 * pezzi della dotazione
	 *
	 * @param livelloDelMondo il livello di riferimento del gioco
	 */
	public static int livelloCasuale(int livelloDelMondo) {
		return Math.max(1, livelloDelMondo - Dado.tira(0, 2));
	}

	/**
	 * Dà al personaggio la dotazione della sua classe
	 *
	 * @param livelloDelMondo il livello di riferimento del gioco, da cui dipende il livello dei pezzi
	 */
	public static void equipaggia(Personaggio personaggio, int livelloDelMondo) {
		for (TipoArtefatto tipo : perClasse(personaggio.getClasse())) {
			int livello = Math.min(personaggio.getLivello(), livelloCasuale(livelloDelMondo));
			Artefatto artefatto = GeneratoreArtefatti.istanza().generaArtefatto(tipo, livello);
			// Di base: niente rarità, incantamenti o nome proprio che il generatore dà ogni tanto
			ArtefattoMD md = artefatto.getModelloDati();
			md.setRarita(TipoRaritaArtefatto.COMUNE);
			md.rimuoviTuttiGliIncantamenti();
			md.setNomeProprio(null);
			if (!personaggio.puoEquipaggiare(artefatto).isPresent()) {
				personaggio.addArtefatto(artefatto);
			}
		}
	}
}
