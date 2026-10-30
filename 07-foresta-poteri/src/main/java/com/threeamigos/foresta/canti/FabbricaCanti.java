package com.threeamigos.foresta.canti;

import com.threeamigos.foresta.personaggi.Personaggio;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import com.threeamigos.foresta.tipi.TipoCanto;

/**
 * Costruisce il canto di quel tipo, per il personaggio che lo canta
 */
public final class FabbricaCanti {

	private FabbricaCanti() {
	}

	/**
	 * Il canto di quel tipo, per il personaggio che lo canta.
	 */
	private static final Map<TipoCanto, Function<Personaggio, Canto>> COSTRUTTORI = new EnumMap<>(TipoCanto.class);

	static {
		COSTRUTTORI.put(TipoCanto.CURATIVO, CantoCurativo::new);
		COSTRUTTORI.put(TipoCanto.IPNOTICO, CantoIpnotico::new);
		COSTRUTTORI.put(TipoCanto.DI_VALORE, CantoDiValore::new);
		COSTRUTTORI.put(TipoCanto.URLO_DELLA_BANSHEE, UrloDellaBanshee::new);
	}

	public static Canto crea(TipoCanto tipo, Personaggio cantore) {
		return COSTRUTTORI.get(tipo).apply(cantore);
	}
}
