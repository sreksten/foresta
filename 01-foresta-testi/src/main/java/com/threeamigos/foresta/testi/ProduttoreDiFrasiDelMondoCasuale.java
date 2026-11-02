package com.threeamigos.foresta.testi;

/**
 * Le frasi a caso dei personaggi del mondo nei fumetti e nei dialoghi ({@code mondo.txt}): per ora come si presenta il
 * protagonista all'inizio della partita. La formula con cui ci si presenta ({@code FORMULA_NOME}) sta in un punto solo e la
 * includono anche le grammatiche che ne hanno bisogno (le locande inglesi).
 */
public final class ProduttoreDiFrasiDelMondoCasuale {

	private static final GrammaticaPigra MONDO = GrammaticaPigra.deiTesti("mondo.txt", false);

	private ProduttoreDiFrasiDelMondoCasuale() {
	}

	/**
	 * Carica subito la grammatica, invece di aspettare il primo uso (vedi {@link ProduttoriDiTesti#precarica()}).
	 *
	 * @throws GrammaticaNonValidaException se la grammatica manca o non è valida
	 */
	public static void precarica() {
		MONDO.get();
	}

	/**
	 * Quello che dice il protagonista quando si presenta all'eremita: il suo nome e che cosa cerca (vedi mondo.txt).
	 *
	 * @param nome il nome con cui si presenta l'eroe
	 */
	public static String presentazioneDelProtagonista(String nome) {
		MONDO.get().addFixedProduction("NOME_EROE", nome);
		return MONDO.get().produce("PRESENTAZIONE_EROE").get(0).trim();
	}
}
