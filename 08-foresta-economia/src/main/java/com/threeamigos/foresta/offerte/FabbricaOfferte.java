package com.threeamigos.foresta.offerte;

import com.threeamigos.foresta.tipi.TipoApproccio;
import com.threeamigos.foresta.tipi.TipoOfferta;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Costruisce l'offerta di ogni TipoOfferta.
 */
public final class FabbricaOfferte {

	private static final Map<TipoOfferta, Function<TipoApproccio, Offerta>> COSTRUTTORI = new EnumMap<>(TipoOfferta.class);

	static {
		COSTRUTTORI.put(TipoOfferta.AIUTO_GRATUITO, AiutoGratuito::new);
		COSTRUTTORI.put(TipoOfferta.AIUTO_MERCENARIO, AiutoMercenario::new);
		COSTRUTTORI.put(TipoOfferta.INCANTESIMI, approccio -> new Incantesimi());
		COSTRUTTORI.put(TipoOfferta.INFORMAZIONI, approccio -> new Informazioni());
		COSTRUTTORI.put(TipoOfferta.MAPPA_FORESTA, approccio -> new MappaForesta());
		COSTRUTTORI.put(TipoOfferta.MAPPA_ZONA, approccio -> new MappaZona());
		COSTRUTTORI.put(TipoOfferta.PASTO, approccio -> new Pasto());
	}

	private FabbricaOfferte() {
	}

	/**
	 * Una nuova offerta di quel tipo, avuta con quell'approccio (conta per gli aiuti: chi è stato corrotto non si unisce per sua scelta).
	 */
	public static Offerta crea(TipoOfferta tipo, TipoApproccio approccio) {
		return COSTRUTTORI.get(tipo).apply(approccio);
	}

	/**
	 * Una nuova offerta di quel tipo, avuta facendo amicizia.
	 */
	public static Offerta crea(TipoOfferta tipo) {
		return crea(tipo, TipoApproccio.AMICIZIA);
	}
}
