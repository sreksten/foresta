package com.threeamigos.foresta.testi;

/**
 * Il ciclo di vita dei produttori di testi a caso di questo modulo: li carica insieme e li azzera quando comincia una partita
 * nuova. I produttori sono {@link ProduttoreDiFiabeCasuale}, {@link ProduttoreDiLocandeCasuale}, {@link ProduttoreDiLuoghiCasuale},
 * {@link ProduttoreDiMissioniCasuale}, {@link ProduttoreDiOroscopiCasuale} e {@link ProduttoreDiSfideCasuale}; ciascuno legge le sue
 * grammatiche una volta sola, la prima volta che si usa. Vedi GrammarBean.md e nomi.md.
 */
public final class ProduttoriDiTesti {

	private ProduttoriDiTesti() {
	}

	/**
	 * Carica subito le grammatiche di tutti i produttori, invece di aspettare il primo uso.
	 *
	 * @throws GrammaticaNonValidaException se una grammatica manca o non è valida
	 */
	public static void precarica() {
		ProduttoreDiFiabeCasuale.precarica();
		ProduttoreDiLocandeCasuale.precarica();
		ProduttoreDiLuoghiCasuale.precarica();
		ProduttoreDiMissioniCasuale.precarica();
		ProduttoreDiOroscopiCasuale.precarica();
		ProduttoreDiSfideCasuale.precarica();
	}

	/**
	 * Azzera le liste esauribili delle grammatiche (i nomi già estratti), così si può ripartire per una partita nuova.
	 */
	public static void resetProduzioni() {
		ProduttoreDiFiabeCasuale.reset();
		ProduttoreDiOroscopiCasuale.reset();
		ProduttoreDiLocandeCasuale.reset();
		GrammarBean.resetStaticProductions();
	}
}
