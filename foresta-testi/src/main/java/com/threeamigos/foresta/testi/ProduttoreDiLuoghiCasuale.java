package com.threeamigos.foresta.testi;

/**
 * I nomi a caso dei luoghi della foresta: i templi ({@code templi.txt}) e le rovine ({@code rovine.txt}).
 */
public final class ProduttoreDiLuoghiCasuale {

	private static final GrammaticaPigra TEMPLI = GrammaticaPigra.deiTesti("templi.txt", true);
	private static final GrammaticaPigra ROVINE = GrammaticaPigra.deiTesti("rovine.txt", true);

	private ProduttoreDiLuoghiCasuale() {
	}

	/**
	 * Carica subito la grammatica, invece di aspettare il primo uso (vedi {@link ProduttoriDiTesti#precarica()}).
	 *
	 * @throws GrammaticaNonValidaException se la grammatica manca o non è valida
	 */
	public static void precarica() {
		TEMPLI.get();
		ROVINE.get();
	}

	/**
	 * Un nome a caso per un tempio, con l'articolo: "il Santuario della Luna" (vedi templi.txt e Tempio.getNome).
	 */
	public static String nomeTempio() {
		return TEMPLI.get().produce("NOME_TEMPIO").get(0).trim();
	}

	/**
	 * Un nome a caso per delle rovine, con l'articolo: "le Vestigia dell'Antico Impero" (vedi rovine.txt e
	 * Rovine.getNome).
	 */
	public static String nomeRovine() {
		return ROVINE.get().produce("NOME_ROVINE").get(0).trim();
	}
}
