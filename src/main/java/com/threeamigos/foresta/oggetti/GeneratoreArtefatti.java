package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.tipi.TipoArtefatto;

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
	 * Carica subito il generatore del gioco e la sua grammatica, che altrimenti si caricherebbero al primo artefatto
	 * (vedi Automa, stato LOGO_INIZIALE).
	 */
	static void precarica() {
		istanza();
	}

	/**
	 * Il danno medio di un'arma di quel livello, prima dello scarto casuale e delle correzioni per tipo
	 * (spadone, bastone): 4 + 2 × livello, ma almeno 11 + livello, perché ai livelli bassi un'arma deve fare
	 * più delle mani nude.
	 */
	static int danniMediArma(int livello) {
		return Math.max(Costanti.ARMA_DANNI_BASE + Costanti.ARMA_DANNI_PER_LIVELLO * livello,
				Costanti.ARMA_DANNI_MINIMI_BASE + Costanti.ARMA_DANNI_MINIMI_PER_LIVELLO * livello);
	}

	/**
	 * Un artefatto del tipo dato; per un ingrediente magico vedi {@link #generaIngrediente}. Ogni tanto un artefatto
	 * incantabile nasce già incantato, tanto più spesso quanto più alto è il livello.
	 */
	Artefatto generaArtefatto(TipoArtefatto tipo, int livello);

	/**
	 * Un artefatto di tipo scelto a caso, ingredienti magici esclusi.
	 */
	Artefatto generaArtefattoCasuale(int livello);

	/**
	 * Un ingrediente magico di tipo scelto a caso (pergamena, gemma, monile, gingillo o sigillo), del livello di
	 * riferimento, con tanti effetti quanto il livello, fino a tre, tutti della specialità del tipo e del grado
	 * adatto al livello: minore fino al 2, normale al 3, maggiore dal 4.
	 */
	Artefatto generaIngrediente(int livello);
}
