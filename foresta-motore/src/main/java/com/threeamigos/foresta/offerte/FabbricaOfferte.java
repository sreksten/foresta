package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.tipi.TipoOfferta;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Costruisce l'offerta di ogni TipoOfferta.
 */
public final class FabbricaOfferte {

	private static final Map<TipoOfferta, Supplier<Offerta>> COSTRUTTORI = new EnumMap<>(TipoOfferta.class);

	static {
		COSTRUTTORI.put(TipoOfferta.AIUTO_GRATUITO, AiutoGratuito::new);
		COSTRUTTORI.put(TipoOfferta.AIUTO_MERCENARIO, AiutoMercenario::new);
		COSTRUTTORI.put(TipoOfferta.INCANTESIMI, Incantesimi::new);
		COSTRUTTORI.put(TipoOfferta.INFORMAZIONI, Informazioni::new);
		COSTRUTTORI.put(TipoOfferta.MAPPA_FORESTA, MappaForesta::new);
		COSTRUTTORI.put(TipoOfferta.MAPPA_ZONA, MappaZona::new);
		COSTRUTTORI.put(TipoOfferta.PASTO, Pasto::new);
	}

	private FabbricaOfferte() {
	}

	public static Offerta crea(TipoOfferta tipo) {
		return COSTRUTTORI.get(tipo).get();
	}
}
