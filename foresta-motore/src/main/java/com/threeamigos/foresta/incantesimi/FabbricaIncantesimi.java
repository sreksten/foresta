package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Costruisce l'incantesimo di ogni TipoIncantesimo e sceglie un incantesimo a caso. I costi li dà l'enum stessa.
 */
public final class FabbricaIncantesimi {

	private static final Map<TipoIncantesimo, Function<Integer, Incantesimo>> COSTRUTTORI = new EnumMap<>(TipoIncantesimo.class);

	static {
		COSTRUTTORI.put(TipoIncantesimo.ARIA, Aria::new);
		COSTRUTTORI.put(TipoIncantesimo.ACQUA, Acqua::new);
		COSTRUTTORI.put(TipoIncantesimo.TERRA, Terra::new);
		COSTRUTTORI.put(TipoIncantesimo.FUOCO, Fuoco::new);
		COSTRUTTORI.put(TipoIncantesimo.FULMINE, Fulmine::new);
		COSTRUTTORI.put(TipoIncantesimo.GELO, Gelo::new);
		COSTRUTTORI.put(TipoIncantesimo.VELENO, Veleno::new);
		COSTRUTTORI.put(TipoIncantesimo.MORTE, Morte::new);
		COSTRUTTORI.put(TipoIncantesimo.RESURREZIONE, Resurrezione::new);
		COSTRUTTORI.put(TipoIncantesimo.ALBA_SACRA, AlbaSacra::new);
		COSTRUTTORI.put(TipoIncantesimo.SILENZIO, Silenzio::new);
		COSTRUTTORI.put(TipoIncantesimo.LUCE_DIVINA, LuceDivina::new);
	}

	private FabbricaIncantesimi() {
	}

	/**
	 * Un nuovo incantesimo di quella classe, del livello di chi lo formula
	 */
	public static Incantesimo crea(TipoIncantesimo classe, int livello) {
		return COSTRUTTORI.get(classe).apply(livello);
	}

	/**
	 * Un incantesimo a caso
	 */
	public static TipoIncantesimo casuale() {
		TipoIncantesimo[] classi = TipoIncantesimo.values();
		return classi[Dado.tira(classi.length) - 1];
	}
}
