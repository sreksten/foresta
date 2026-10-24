package com.threeamigos.foresta.testi;

import java.util.List;

/**
 * Le fiabe a caso che il locandiere racconta (vedi {@code fiabe.txt}).
 */
public final class ProduttoreDiFiabeCasuale {

	private static final GrammaticaPigra FIABE = GrammaticaPigra.deiTesti("fiabe.txt", true);

	private ProduttoreDiFiabeCasuale() {
	}

	/**
	 * Carica subito la grammatica, invece di aspettare il primo uso (vedi {@link ProduttoriDiTesti#precarica()}).
	 *
	 * @throws GrammaticaNonValidaException se la grammatica manca o non è valida
	 */
	public static void precarica() {
		FIABE.get();
	}

	/**
	 * Una fiaba a caso, a paragrafi.
	 */
	public static List<String> fiaba() {
		List<String> fiaba = FIABE.get().produce();
		FIABE.get().reset();
		return fiaba;
	}

	/**
	 * Azzera le liste esauribili della grammatica (vedi {@link ProduttoriDiTesti#resetProduzioni()}).
	 */
	static void reset() {
		FIABE.get().reset();
	}
}
