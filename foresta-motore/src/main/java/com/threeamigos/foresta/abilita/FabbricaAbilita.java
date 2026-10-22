package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.personaggi.Personaggio;
import com.threeamigos.foresta.tipi.TipoAbilita;

/**
 * Costruisce l'abilità di quel tipo, per il personaggio che la usa
 */
public final class FabbricaAbilita {

	private FabbricaAbilita() {
	}

	/**
	 * L'abilità di quel tipo, per il personaggio che la usa.
	 */
	public static Abilita crea(TipoAbilita tipo, Personaggio utilizzatore) {
		switch (tipo) {
			case SCACCIARE_I_NON_MORTI:
				return new ScacciareINonMorti(utilizzatore);
			case IMPOSIZIONE_DELLE_MANI:
				return new ImposizioneDelleMani(utilizzatore);
			case RADICI:
				return new Radici(utilizzatore);
			case SCIAME:
				return new Sciame(utilizzatore);
			case PELLE_DI_CORTECCIA:
				return new PelleDiCorteccia(utilizzatore);
			case RIGENERAZIONE:
				return new Rigenerazione(utilizzatore);
			default:
				throw new IllegalArgumentException("TipoAbilita non gestito: " + tipo);
		}
	}
}
