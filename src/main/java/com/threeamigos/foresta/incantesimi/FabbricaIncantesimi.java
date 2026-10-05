package com.threeamigos.foresta.incantesimi;

import com.threeamigos.foresta.motore.Costanti;
import com.threeamigos.foresta.motore.Dado;
import com.threeamigos.foresta.tipi.ClasseIncantesimo;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Costruisce l'incantesimo di ogni ClasseIncantesimo, e dà quel che dipende dalle regole del gioco: i costi (da
 * Costanti; il costo di lancio lo dice l'incantesimo stesso) e la scelta di un incantesimo a caso.
 */
public final class FabbricaIncantesimi {

	private static final Map<ClasseIncantesimo, Function<Integer, Incantesimo>> COSTRUTTORI = new EnumMap<>(ClasseIncantesimo.class);
	private static final Map<ClasseIncantesimo, Integer> COSTI_ACQUISTO = new EnumMap<>(ClasseIncantesimo.class);

	static {
		COSTRUTTORI.put(ClasseIncantesimo.ARIA, Aria::new);
		COSTRUTTORI.put(ClasseIncantesimo.ACQUA, Acqua::new);
		COSTRUTTORI.put(ClasseIncantesimo.TERRA, Terra::new);
		COSTRUTTORI.put(ClasseIncantesimo.FUOCO, Fuoco::new);
		COSTRUTTORI.put(ClasseIncantesimo.FULMINE, Fulmine::new);
		COSTRUTTORI.put(ClasseIncantesimo.GELO, Gelo::new);
		COSTRUTTORI.put(ClasseIncantesimo.VELENO, Veleno::new);
		COSTRUTTORI.put(ClasseIncantesimo.MORTE, Morte::new);
		COSTRUTTORI.put(ClasseIncantesimo.RESURREZIONE, Resurrezione::new);
		COSTRUTTORI.put(ClasseIncantesimo.ALBA_SACRA, AlbaSacra::new);
		COSTI_ACQUISTO.put(ClasseIncantesimo.ARIA, Costanti.INCANTESIMO_ARIA_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(ClasseIncantesimo.ACQUA, Costanti.INCANTESIMO_ACQUA_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(ClasseIncantesimo.TERRA, Costanti.INCANTESIMO_TERRA_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(ClasseIncantesimo.FUOCO, Costanti.INCANTESIMO_FUOCO_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(ClasseIncantesimo.FULMINE, Costanti.INCANTESIMO_FULMINE_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(ClasseIncantesimo.GELO, Costanti.INCANTESIMO_GELO_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(ClasseIncantesimo.VELENO, Costanti.INCANTESIMO_VELENO_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(ClasseIncantesimo.MORTE, Costanti.INCANTESIMO_MORTE_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(ClasseIncantesimo.RESURREZIONE, Costanti.INCANTESIMO_RESURREZIONE_COSTO_ACQUISTO);
		COSTI_ACQUISTO.put(ClasseIncantesimo.ALBA_SACRA, Costanti.INCANTESIMO_ALBA_SACRA_COSTO_ACQUISTO);
	}

	private FabbricaIncantesimi() {
	}

	/**
	 * Un nuovo incantesimo di quella classe, del livello di chi lo formula
	 */
	public static Incantesimo crea(ClasseIncantesimo classe, int livello) {
		return COSTRUTTORI.get(classe).apply(livello);
	}

	/**
	 * Quante monete costa acquistarne una pergamena
	 */
	public static int costoAcquisto(ClasseIncantesimo classe) {
		return COSTI_ACQUISTO.get(classe);
	}

	/**
	 * Quanta magia costa lanciarlo: lo dice l'incantesimo, che non dipende dal livello per questo
	 */
	public static int costoLancio(ClasseIncantesimo classe) {
		return crea(classe, 1).getCostoLancio();
	}

	/**
	 * Un incantesimo a caso
	 */
	public static ClasseIncantesimo casuale() {
		ClasseIncantesimo[] classi = ClasseIncantesimo.values();
		return classi[Dado.tira(classi.length) - 1];
	}
}
