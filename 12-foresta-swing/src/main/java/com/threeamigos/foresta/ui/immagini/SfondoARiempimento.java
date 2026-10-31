package com.threeamigos.foresta.ui.immagini;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Lo sfondo di un'icona in ricarica: la cornice con l'interno vuoto, che si riempie dal basso verso l'alto in proporzione ai turni passati, con un colore
 * che va dal rosso scuro al verde scuro passando per il giallo scuro (come la barra della salute: così a metà non viene fuori un marrone); la parte
 * non ancora riempita è nera (il grigio si distingueva poco dal riempimento). Sopra ci va il disegno dell'icona (vedi TipoIcona.componi).
 * <p>
 * L'interno è la zona trasparente della cornice raggiungibile dal suo centro: gli angoli fuori dal bordo restano trasparenti.
 */
public final class SfondoARiempimento {

	private static final int ROSSO_SCURO = 0x8C0000;
	// Il giallo è scuro quanto il rosso (stesso rosso, verde alla pari): da lì il rosso cala e il verde scende al suo valore
	private static final int GIALLO_SCURO = 0x8C8C00;
	private static final int VERDE_SCURO = 0x005200;
	private static final int NERO = 0x000000;

	private SfondoARiempimento() {
	}

	/**
	 * Lo sfondo con la ricarica a quel punto.
	 *
	 * @param cornice la cornice con l'interno trasparente
	 * @param turniPassati da quanti turni è in ricarica, contando quello dell'uso: da 1 a {@code durata}
	 * @param durata i turni che dura la ricarica in tutto: lo sfondo si riempie di {@code turniPassati/(durata+1)}, e alla fine dei turni l'icona torna normale
	 */
	public static BufferedImage crea(BufferedImage cornice, int turniPassati, int durata) {
		int larghezza = cornice.getWidth();
		int altezza = cornice.getHeight();
		boolean[][] interno = interno(cornice);
		int primaRiga = altezza;
		int ultimaRiga = -1;
		for (int y = 0; y < altezza; y++) {
			for (int x = 0; x < larghezza; x++) {
				if (interno[x][y]) {
					primaRiga = Math.min(primaRiga, y);
					ultimaRiga = Math.max(ultimaRiga, y);
				}
			}
		}
		double quota = (double) turniPassati / (durata + 1);
		int righeInterne = ultimaRiga - primaRiga + 1;
		int righeRiempite = (int) Math.round(righeInterne * quota);
		int coloreRiempimento = coloreDelRiempimento(quota);

		BufferedImage sfondo = new BufferedImage(larghezza, altezza, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < altezza; y++) {
			for (int x = 0; x < larghezza; x++) {
				if (!interno[x][y]) {
					sfondo.setRGB(x, y, cornice.getRGB(x, y));
				} else {
					boolean riempito = ultimaRiga - y < righeRiempite;
					sfondo.setRGB(x, y, 0xFF000000 | (riempito ? coloreRiempimento : NERO));
				}
			}
		}
		return sfondo;
	}

	/**
	 * I pixel dell'interno: quelli trasparenti raggiungibili dal centro senza attraversare il bordo.
	 */
	private static boolean[][] interno(BufferedImage cornice) {
		int larghezza = cornice.getWidth();
		int altezza = cornice.getHeight();
		boolean[][] interno = new boolean[larghezza][altezza];
		int partenzaX = larghezza / 2;
		int partenzaY = altezza / 2;
		if (!isTrasparente(cornice, partenzaX, partenzaY)) {
			return interno;
		}
		Deque<int[]> daVisitare = new ArrayDeque<>();
		daVisitare.push(new int[] {partenzaX, partenzaY});
		interno[partenzaX][partenzaY] = true;
		int[][] vicini = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
		while (!daVisitare.isEmpty()) {
			int[] punto = daVisitare.pop();
			for (int[] vicino : vicini) {
				int x = punto[0] + vicino[0];
				int y = punto[1] + vicino[1];
				if (x >= 0 && y >= 0 && x < larghezza && y < altezza && !interno[x][y] && isTrasparente(cornice, x, y)) {
					interno[x][y] = true;
					daVisitare.push(new int[] {x, y});
				}
			}
		}
		return interno;
	}

	private static boolean isTrasparente(BufferedImage immagine, int x, int y) {
		return (immagine.getRGB(x, y) >>> 24) == 0;
	}

	/**
	 * Il colore del riempimento a quella quota: dal rosso scuro (0) al giallo scuro (metà) al verde scuro (1).
	 */
	static int coloreDelRiempimento(double quota) {
		return quota < 0.5d ? interpola(ROSSO_SCURO, GIALLO_SCURO, quota * 2.0d) : interpola(GIALLO_SCURO, VERDE_SCURO, (quota - 0.5d) * 2.0d);
	}

	/**
	 * Il colore a quella frazione del cammino fra due colori: si toglie un poco dell'uno e se ne aggiunge un poco dell'altro.
	 */
	private static int interpola(int da, int a, double quota) {
		int rosso = (int) Math.round(((da >> 16) & 0xFF) * (1 - quota) + ((a >> 16) & 0xFF) * quota);
		int verde = (int) Math.round(((da >> 8) & 0xFF) * (1 - quota) + ((a >> 8) & 0xFF) * quota);
		int blu = (int) Math.round((da & 0xFF) * (1 - quota) + (a & 0xFF) * quota);
		return (rosso << 16) | (verde << 8) | blu;
	}
}
