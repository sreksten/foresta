package com.threeamigos.foresta.abilita;

import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
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
	private static final Map<TipoAbilita, Function<Personaggio, Abilita>> COSTRUTTORI = new EnumMap<>(TipoAbilita.class);

	static {
		COSTRUTTORI.put(TipoAbilita.SCACCIARE_I_NON_MORTI, ScacciareINonMorti::new);
		COSTRUTTORI.put(TipoAbilita.IMPOSIZIONE_DELLE_MANI, ImposizioneDelleMani::new);
		COSTRUTTORI.put(TipoAbilita.RADICI, Radici::new);
		COSTRUTTORI.put(TipoAbilita.SCIAME, Sciame::new);
		COSTRUTTORI.put(TipoAbilita.PELLE_DI_CORTECCIA, PelleDiCorteccia::new);
		COSTRUTTORI.put(TipoAbilita.RIGENERAZIONE, Rigenerazione::new);
		COSTRUTTORI.put(TipoAbilita.ALBA_SACRA, AlbaSacra::new);
		COSTRUTTORI.put(TipoAbilita.LUCE_DIVINA, LuceDivina::new);
		COSTRUTTORI.put(TipoAbilita.DARDO_ARCANO, DardoArcano::new);
	}

	public static Abilita crea(TipoAbilita tipo, Personaggio utilizzatore) {
		return COSTRUTTORI.get(tipo).apply(utilizzatore);
	}
}
