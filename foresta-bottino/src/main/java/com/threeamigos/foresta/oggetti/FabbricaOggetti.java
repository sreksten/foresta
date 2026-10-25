package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoBottino;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Costruisce l'oggetto di ogni TipoBottino generabile (vedi TipoBottino.isGenerabile).
 */
public final class FabbricaOggetti {

	private static final Map<TipoBottino, Supplier<Bottino>> COSTRUTTORI = new EnumMap<>(TipoBottino.class);

	static {
		COSTRUTTORI.put(TipoBottino.ANELLO, Anello::new);
		COSTRUTTORI.put(TipoBottino.COFANO, Cofano::new);
		COSTRUTTORI.put(TipoBottino.CORONA, Corona::new);
		COSTRUTTORI.put(TipoBottino.PIETRA_PREZIOSA, PietraPreziosa::new);
		COSTRUTTORI.put(TipoBottino.MONETA, Moneta::new);
		// Le spade, gli scudi, gli elmi...: un artefatto casuale di quella forma, al livello del mondo
		for (FormaDelBottino forma : FormaDelBottino.values()) {
			COSTRUTTORI.put(forma.getTipoBottino(), () -> BottinoArtefatto.nuovo(forma));
		}
	}

	private FabbricaOggetti() {
	}

	/**
	 * Un oggetto nuovo di quel tipo.
	 *
	 * @throws IllegalArgumentException se il tipo non si genera così (per esempio gli oggetti delle missioni)
	 */
	public static Bottino crea(TipoBottino tipo) {
		Supplier<Bottino> costruttore = COSTRUTTORI.get(tipo);
		if (costruttore == null) {
			throw new IllegalArgumentException(tipo + " non si genera così");
		}
		return costruttore.get();
	}
}
