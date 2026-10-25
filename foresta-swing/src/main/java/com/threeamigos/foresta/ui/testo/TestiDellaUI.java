package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.testi.Testi;

/**
 * Le scritte della UI che non vengono da un'enum, lette da {@code ui_<lingua>.properties} (in italiano se la lingua scelta
 * non ha la voce). Nel testo {@code {0}}, {@code {1}}... sono i valori che la UI inserisce.
 */
public final class TestiDellaUI {

	private static final String FILE = "ui";

	private TestiDellaUI() {
	}

	/**
	 * Il testo con quella chiave, con i valori al posto di {0}, {1}...; se la chiave manca, la chiave stessa, così
	 * l'errore si nota subito.
	 */
	public static String get(String chiave, Object... valori) {
		String testo = Testi.get(FILE, chiave).orElse(chiave);
		for (int i = 0; i < valori.length; i++) {
			testo = testo.replace("{" + i + "}", String.valueOf(valori[i]));
		}
		return testo;
	}
}
