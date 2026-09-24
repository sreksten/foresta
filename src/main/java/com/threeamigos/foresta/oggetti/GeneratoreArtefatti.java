package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.modellodati.TipoArtefatto;

/**
 * Genera artefatti e pergamene a caso, per il loot e per i negozi.
 * Il livello da passare di solito è quello di riferimento del gioco, Statistiche.getLivello()
 * (lo stesso dei mostri). Oggi i nomi vengono da piccole tabelle interne
 * ({@link GeneratoreArtefattiTabelle}); poi arriverà una grammatica, sul modello delle locande,
 * senza cambiare chi chiama.
 */
public interface GeneratoreArtefatti {

	static GeneratoreArtefatti istanza() {
		return GeneratoreArtefattiTabelle.ISTANZA;
	}

	/**
	 * Un artefatto del tipo dato; per TipoArtefatto.INCANTAMENTO una pergamena. Ogni tanto un artefatto
	 * incantabile nasce già incantato, tanto più spesso quanto più alto è il livello.
	 */
	Artefatto generaArtefatto(TipoArtefatto tipo, int livello);

	/**
	 * Un artefatto di tipo scelto a caso, pergamene escluse.
	 */
	Artefatto generaArtefattoCasuale(int livello);

	/**
	 * Una pergamena di livello da 1 a 3, con tanti effetti quanto il suo livello (incantamenti e modificatori
	 * di attributo scelti a caso), del grado adatto al livello di riferimento.
	 */
	Artefatto generaPergamena(int livello);
}
