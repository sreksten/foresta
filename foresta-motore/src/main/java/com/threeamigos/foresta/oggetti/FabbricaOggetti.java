package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoOggetto;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Costruisce l'oggetto di ogni TipoOggetto generabile (vedi TipoOggetto.isGenerabile).
 */
public final class FabbricaOggetti {

	private static final Map<TipoOggetto, Supplier<Bottino>> COSTRUTTORI = new EnumMap<>(TipoOggetto.class);

	static {
		COSTRUTTORI.put(TipoOggetto.ANELLO, Anello::new);
		COSTRUTTORI.put(TipoOggetto.COFANO, Cofano::new);
		COSTRUTTORI.put(TipoOggetto.CORONA, Corona::new);
		COSTRUTTORI.put(TipoOggetto.PIETRA_PREZIOSA, PietraPreziosa::new);
		COSTRUTTORI.put(TipoOggetto.MONETA, Moneta::new);
		// Le spade, gli scudi, gli elmi...: un artefatto casuale di quella forma, al livello del mondo
		for (FormaDelBottino forma : FormaDelBottino.values()) {
			COSTRUTTORI.put(forma.getTipoOggetto(), () -> BottinoArtefatto.nuovo(forma));
		}
	}

	private FabbricaOggetti() {
	}

	/**
	 * Un oggetto nuovo di quel tipo.
	 *
	 * @throws IllegalArgumentException se il tipo non si genera così (per esempio gli oggetti delle missioni)
	 */
	public static Bottino crea(TipoOggetto tipo) {
		Supplier<Bottino> costruttore = COSTRUTTORI.get(tipo);
		if (costruttore == null) {
			throw new IllegalArgumentException(tipo + " non si genera così");
		}
		return costruttore.get();
	}
}
