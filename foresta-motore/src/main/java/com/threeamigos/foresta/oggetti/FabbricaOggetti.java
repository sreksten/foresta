package com.threeamigos.foresta.oggetti;

import com.threeamigos.foresta.tipi.TipoOggetto;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Costruisce l'oggetto di ogni TipoOggetto generabile (vedi TipoOggetto.isGenerabile).
 */
public final class FabbricaOggetti {

	private static final Map<TipoOggetto, Supplier<Oggetto>> COSTRUTTORI = new EnumMap<>(TipoOggetto.class);

	static {
		COSTRUTTORI.put(TipoOggetto.ANELLO, Anello::new);
		COSTRUTTORI.put(TipoOggetto.COFANO, Cofano::new);
		COSTRUTTORI.put(TipoOggetto.CORONA, Corona::new);
		COSTRUTTORI.put(TipoOggetto.PIETRA_PREZIOSA, PietraPreziosa::new);
		COSTRUTTORI.put(TipoOggetto.MONETA, Moneta::new);
		COSTRUTTORI.put(TipoOggetto.SCUDO, Scudo::new);
		COSTRUTTORI.put(TipoOggetto.SPADA, Spada::new);
		COSTRUTTORI.put(TipoOggetto.SPADONE, Spadone::new);
		COSTRUTTORI.put(TipoOggetto.ELMO, Elmo::new);
		COSTRUTTORI.put(TipoOggetto.MASCHERA, Maschera::new);
		COSTRUTTORI.put(TipoOggetto.ARMATURA, Armatura::new);
		COSTRUTTORI.put(TipoOggetto.SCHINIERI, Schinieri::new);
	}

	private FabbricaOggetti() {
	}

	public static Oggetto crea(TipoOggetto tipo) {
		Supplier<Oggetto> costruttore = COSTRUTTORI.get(tipo);
		if (costruttore == null) {
			throw new IllegalArgumentException(tipo + " non si genera così");
		}
		return costruttore.get();
	}
}
