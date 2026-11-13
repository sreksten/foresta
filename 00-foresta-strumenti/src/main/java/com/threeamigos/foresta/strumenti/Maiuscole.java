package com.threeamigos.foresta.strumenti;

/**
 * La maiuscola all'inizio di un testo: "il Tempio del Sole" diventa "Il Tempio del Sole". Sta qui, nel modulo più basso,
 * perché serve a moduli che non si vedono fra loro (i testi, gli eventi, le missioni, l'interfaccia).
 */
public final class Maiuscole {

	// Una classe di soli metodi statici: non si istanzia
	private Maiuscole() {
	}

	/**
	 * Il testo con il primo carattere in maiuscolo e il resto com'è; null e il testo vuoto restano come sono.
	 */
	public static String inizialeMaiuscola(String testo) {
		if (testo == null || testo.isEmpty()) {
			return testo;
		}
		return Character.toUpperCase(testo.charAt(0)) + testo.substring(1);
	}
}
