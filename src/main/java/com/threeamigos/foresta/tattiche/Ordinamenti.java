package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.modellodati.tattiche.OrdinamentoMD;
import com.threeamigos.foresta.motore.CalcolatoreCombattimento;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.List;

/**
 * Sceglie uno fra i candidati con un {@link OrdinamentoMD} (tattiche_di_combattimento_automatiche.md §11.6). A parità di misura vince il primo
 * dell'elenco, quindi la scelta è ripetibile; solo A_CASO tira il dado.
 */
public final class Ordinamenti {

	private Ordinamenti() {
	}

	/**
	 * @return il candidato scelto, o null se non ce ne sono
	 */
	public static Personaggio scegli(List<Personaggio> candidati, OrdinamentoMD ordinamento) {
		if (candidati.isEmpty()) {
			return null;
		}
		switch (ordinamento.getTipo()) {
			case PRIMO:
				return candidati.get(0);
			case A_CASO:
				return candidati.get(Dado.tiraAncheAUnaFaccia(candidati.size()) - 1);
			default:
				return conLaMisuraMigliore(candidati, ordinamento);
		}
	}

	private static Personaggio conLaMisuraMigliore(List<Personaggio> candidati, OrdinamentoMD ordinamento) {
		Personaggio migliore = candidati.get(0);
		double misuraMigliore = misura(migliore, ordinamento);
		for (int i = 1; i < candidati.size(); i++) {
			double misura = misura(candidati.get(i), ordinamento);
			if (misura > misuraMigliore) {
				migliore = candidati.get(i);
				misuraMigliore = misura;
			}
		}
		return migliore;
	}

	/**
	 * Il valore che l'ordinamento massimizza (per i "meno", il suo opposto)
	 */
	private static double misura(Personaggio candidato, OrdinamentoMD ordinamento) {
		switch (ordinamento.getTipo()) {
			case MENO_SALUTE:
				return -candidato.getSalute();
			case PIU_SALUTE:
				return candidato.getSalute();
			case LIVELLO_PIU_BASSO:
				return -candidato.getLivello();
			case LIVELLO_PIU_ALTO:
				return candidato.getLivello();
			case PIU_DEBOLE_A:
				return CalcolatoreCombattimento.moltiplicatoreAtteso(candidato, ordinamento.getTipoDanno());
			case PIU_RESISTENTE_A:
				return -CalcolatoreCombattimento.moltiplicatoreAtteso(candidato, ordinamento.getTipoDanno());
			case PIU_MAGIA:
				return candidato.getMagia();
			case MENO_MAGIA:
				return -candidato.getMagia();
			default:
				throw new IllegalStateException("OrdinamentoMD senza misura: " + ordinamento);
		}
	}
}
