package com.threeamigos.foresta.ui.sfx;

import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;

import java.util.Random;

/**
 * Genera una nuvola procedurale, senza toolkit: una manciata di cerchi sfumati sovrapposti (ellissi un po' schiacciate), su uno sfondo trasparente. Ogni
 * "soffio" ha una sfumatura radiale in tre punti (bianco semitrasparente al centro, un po' più trasparente e freddo a otto decimi del raggio, del tutto
 * trasparente sul bordo) e si compone sugli altri come un normale disegno con trasparenza (sorgente sopra). Il risultato è un'{@link ImmagineDiPixel}, che
 * ogni interfaccia trasforma nella sua immagine.
 */
public final class GeneratoreDiNuvole {

	// Le tre fermate della sfumatura: distanza dal centro (in raggi) e colore ARGB
	private static final double[] DISTANZE = {0.0, 0.8, 1.0};
	private static final int[][] COLORI = {{180, 255, 255, 255}, {90, 240, 240, 245}, {0, 255, 255, 255}};

	private GeneratoreDiNuvole() {
	}

	/**
	 * @param larghezza larghezza dell'immagine
	 * @param altezza   altezza dell'immagine
	 * @return la nuvola su sfondo trasparente (ARGB, non premoltiplicato)
	 */
	public static ImmagineDiPixel genera(int larghezza, int altezza, Random caso) {
		int[] pixel = new int[larghezza * altezza];
		// Numero di "soffici cerchi" che comporranno la nuvola (tra 5 e 8)
		int numeroDiSoffi = 5 + caso.nextInt(4);
		// Una nuvola minuscola (un'area conosciuta stretta) non deve dare limiti nulli a nextInt né un raggio nullo alla sfumatura
		int lato = Math.max(4, Math.min(larghezza, altezza));
		for (int i = 0; i < numeroDiSoffi; i++) {
			// Dimensioni casuali per ogni soffio
			int raggio = lato / 3 + caso.nextInt(lato / 4);
			// I cerchi stanno principalmente verso il centro dell'immagine
			int centroX = larghezza / 2 + (caso.nextInt(Math.max(1, larghezza / 2)) - larghezza / 4);
			int centroY = altezza / 2 + (caso.nextInt(Math.max(1, altezza / 4)) - altezza / 8);
			// Un'ellisse leggermente schiacciata sull'asse y, per farla sembrare realistica
			double semiLarghezza = raggio;
			double semiAltezza = raggio * 1.4 / 2;
			componi(pixel, larghezza, altezza, centroX, centroY, raggio, semiLarghezza, semiAltezza);
		}
		return new ImmagineDiPixel(larghezza, altezza, pixel);
	}

	private static void componi(int[] pixel, int larghezza, int altezza, int centroX, int centroY, int raggio, double semiLarghezza, double semiAltezza) {
		int daX = Math.max(0, (int) Math.floor(centroX - semiLarghezza));
		int aX = Math.min(larghezza - 1, (int) Math.ceil(centroX + semiLarghezza));
		int daY = Math.max(0, (int) Math.floor(centroY - semiAltezza));
		int aY = Math.min(altezza - 1, (int) Math.ceil(centroY + semiAltezza));
		for (int y = daY; y <= aY; y++) {
			for (int x = daX; x <= aX; x++) {
				double dx = x + 0.5 - centroX;
				double dy = y + 0.5 - centroY;
				if ((dx * dx) / (semiLarghezza * semiLarghezza) + (dy * dy) / (semiAltezza * semiAltezza) > 1.0) {
					continue;
				}
				double distanza = Math.sqrt(dx * dx + dy * dy) / raggio;
				pixel[y * larghezza + x] = sopra(sfumatura(distanza), pixel[y * larghezza + x]);
			}
		}
	}

	/**
	 * Il colore della sfumatura a quella distanza dal centro (in raggi), interpolando fra le fermate canale per canale; oltre l'ultima resta l'ultimo
	 */
	static int sfumatura(double distanza) {
		if (distanza >= DISTANZE[DISTANZE.length - 1]) {
			return impacchetta(COLORI[COLORI.length - 1]);
		}
		for (int i = 1; i < DISTANZE.length; i++) {
			if (distanza <= DISTANZE[i]) {
				double t = (distanza - DISTANZE[i - 1]) / (DISTANZE[i] - DISTANZE[i - 1]);
				int[] colore = new int[4];
				for (int canale = 0; canale < 4; canale++) {
					colore[canale] = (int) Math.round(COLORI[i - 1][canale] + t * (COLORI[i][canale] - COLORI[i - 1][canale]));
				}
				return impacchetta(colore);
			}
		}
		return impacchetta(COLORI[0]);
	}

	private static int impacchetta(int[] argb) {
		return (argb[0] << 24) | (argb[1] << 16) | (argb[2] << 8) | argb[3];
	}

	/**
	 * Compone un pixel sopra un altro (sorgente sopra, ARGB non premoltiplicati)
	 */
	static int sopra(int sorgente, int sotto) {
		double alfaSorgente = (sorgente >>> 24) / 255.0;
		double alfaSotto = (sotto >>> 24) / 255.0;
		double alfa = alfaSorgente + alfaSotto * (1 - alfaSorgente);
		if (alfa <= 0) {
			return 0;
		}
		int risultato = (int) Math.round(alfa * 255) << 24;
		for (int spostamento = 16; spostamento >= 0; spostamento -= 8) {
			double s = (sorgente >> spostamento) & 0xFF;
			double b = (sotto >> spostamento) & 0xFF;
			risultato |= (int) Math.round((s * alfaSorgente + b * alfaSotto * (1 - alfaSorgente)) / alfa) << spostamento;
		}
		return risultato;
	}
}
