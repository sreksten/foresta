package com.threeamigos.foresta.ui.sfx;

import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;

/**
 * La testa luminosa di un corridore del logo, come immagine: un alone radiale morbido (bianco pieno al centro, bianco semitrasparente a un po' meno
 * di meta' raggio, trasparente al bordo) con sopra un piccolo cerchio bianco pieno, per un nucleo netto. E' quel che in Swing si disegna con un
 * gradiente radiale; per i toolkit che non lo hanno (libGDX) basta mettere questa immagine con il suo centro sul corridore.
 */
public final class TestaDellaCometa {

	/** Il raggio dell'alone, in pixel */
	public static final int RAGGIO = 16;
	private static final double RAGGIO_DEL_NUCLEO = 3.0;
	// Le tappe del gradiente: posizione (frazione del raggio) e opacita' (0-255)
	private static final double[] POSIZIONE = {0.0, 0.4, 1.0};
	private static final double[] ALPHA = {255, 110, 0};

	private TestaDellaCometa() {
	}

	/**
	 * L'immagine, quadrata e larga due raggi, con il centro della testa nel centro dell'immagine
	 */
	public static ImmagineDiPixel immagine() {
		int lato = 2 * RAGGIO;
		int[] argb = new int[lato * lato];
		for (int y = 0; y < lato; y++) {
			for (int x = 0; x < lato; x++) {
				double d = Math.hypot(x + 0.5 - RAGGIO, y + 0.5 - RAGGIO);
				double alone = alone(d / RAGGIO);
				double nucleo = Math.max(0.0, Math.min(1.0, RAGGIO_DEL_NUCLEO - d + 0.5));
				// il nucleo bianco pieno sopra l'alone
				double alpha = alone + nucleo * (255 - alone);
				argb[y * lato + x] = (int) Math.round(alpha) << 24 | 0xFFFFFF;
			}
		}
		return new ImmagineDiPixel(lato, lato, argb);
	}

	/** L'opacita' (0-255) dell'alone a quella frazione del raggio, interpolando fra le tappe */
	static double alone(double frazione) {
		if (frazione >= POSIZIONE[POSIZIONE.length - 1]) {
			return ALPHA[ALPHA.length - 1];
		}
		for (int i = 1; i < POSIZIONE.length; i++) {
			if (frazione < POSIZIONE[i]) {
				double t = (frazione - POSIZIONE[i - 1]) / (POSIZIONE[i] - POSIZIONE[i - 1]);
				return ALPHA[i - 1] + (ALPHA[i] - ALPHA[i - 1]) * t;
			}
		}
		return ALPHA[ALPHA.length - 1];
	}
}
