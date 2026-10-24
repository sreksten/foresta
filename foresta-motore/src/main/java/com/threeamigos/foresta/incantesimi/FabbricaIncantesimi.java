package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.strumenti.dado.Dado;
import com.threeamigos.foresta.tipi.Costanti;
import com.threeamigos.foresta.tipi.TipoIncantesimo;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Costruisce l'incantesimo di ogni ClasseIncantesimo, e dà quel che dipende dalle regole del gioco: i costi (da
 * Costanti; il costo di lancio lo dice l'incantesimo stesso) e la scelta di un incantesimo a caso.
 */
public final class FabbricaIncantesimi {

	private static final Map<TipoIncantesimo, Function<Integer, Incantesimo>> COSTRUTTORI = new EnumMap<>(TipoIncantesimo.class);
	private static final Map<TipoIncantesimo, Integer> COSTI_ACQUISTO = new EnumMap<>(TipoIncantesimo.class);

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
		COSTI_ACQUISTO.put(TipoIncantesimo.ARIA, Costanti.INCANTESIMO_ARIA_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(TipoIncantesimo.ACQUA, Costanti.INCANTESIMO_ACQUA_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(TipoIncantesimo.TERRA, Costanti.INCANTESIMO_TERRA_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(TipoIncantesimo.FUOCO, Costanti.INCANTESIMO_FUOCO_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(TipoIncantesimo.FULMINE, Costanti.INCANTESIMO_FULMINE_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(TipoIncantesimo.GELO, Costanti.INCANTESIMO_GELO_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(TipoIncantesimo.VELENO, Costanti.INCANTESIMO_VELENO_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(TipoIncantesimo.MORTE, Costanti.INCANTESIMO_MORTE_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(TipoIncantesimo.RESURREZIONE, Costanti.INCANTESIMO_RESURREZIONE_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(TipoIncantesimo.ALBA_SACRA, Costanti.INCANTESIMO_ALBA_SACRA_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(TipoIncantesimo.SILENZIO, Costanti.INCANTESIMO_SILENZIO_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(TipoIncantesimo.LUCE_DIVINA, Costanti.INCANTESIMO_LUCE_DIVINA_COSTO_ACQUISTO);
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
	 * Quante monete costa acquistarne una pergamena
	 */
	public static int costoAcquisto(TipoIncantesimo classe) {
		return COSTI_ACQUISTO.get(classe);
	}

	/**
	 * Quanta magia costa lanciarlo: lo dice l'incantesimo, che non dipende dal livello per questo
	 */
	public static int costoLancio(TipoIncantesimo classe) {
		return crea(classe, 1).getCostoLancio();
	}

	/**
	 * Un incantesimo a caso
	 */
	public static TipoIncantesimo casuale() {
		TipoIncantesimo[] classi = TipoIncantesimo.values();
		return classi[Dado.tira(classi.length) - 1];
	}
}
