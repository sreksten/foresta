package com.threeamigos.foresta.tattiche;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoAttributo;

/**
 * L'astuzia di un personaggio per le tattiche (tattiche_di_combattimento_automatiche.md §4, formula B): l'intelligenza massima della sua classe per
 * <code>min(1, ASTUZIA_PARTENZA + ASTUZIA_PER_LIVELLO × (livello − 1))</code>. Un mostro tonto all'inizio diventa più ostico col salire del livello del mondo;
 * il massimo di classe non è l'INTELLIGENZA vera del personaggio, che la formula del livello e la distribuzione dei punti spostano.
 */
public final class Astuzia {

	private Astuzia() {
	}

	public static double di(Personaggio personaggio) {
		double massimo = personaggio.getMaxStatistica(TipoAttributo.INTELLIGENZA);
		double rampa = Math.min(1.0d, Costanti.ASTUZIA_PARTENZA + Costanti.ASTUZIA_PER_LIVELLO * (personaggio.getLivello() - 1));
		return massimo * rampa;
	}
}
