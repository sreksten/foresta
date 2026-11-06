package com.threeamigos.foresta.ui.immagini;

/**
 * Le sagome di un'immagine, in pixel: dove l'immagine ha qualcosa, un solo colore pieno, con la stessa trasparenza. Servono alle silhouette dei personaggi
 * ancora sconosciuti (il bestiario).
 */
public final class SagomeDiPixel {

	private SagomeDiPixel() {
	}

	/**
	 * La sagoma dell'immagine di quel colore (RGB; l'alfa di ogni pixel resta quella dell'immagine)
	 */
	public static ImmagineDiPixel sagoma(ImmagineDiPixel immagine, int rgb) {
		int[] pixel = new int[immagine.getLarghezza() * immagine.getAltezza()];
		for (int y = 0; y < immagine.getAltezza(); y++) {
			for (int x = 0; x < immagine.getLarghezza(); x++) {
				int alfa = immagine.getArgb(x, y) >>> 24;
				pixel[y * immagine.getLarghezza() + x] = (alfa << 24) | (rgb & 0xFFFFFF);
			}
		}
		return new ImmagineDiPixel(immagine.getLarghezza(), immagine.getAltezza(), pixel);
	}

	/**
	 * L'immagine rovesciata orizzontalmente
	 */
	public static ImmagineDiPixel specchia(ImmagineDiPixel immagine) {
		int larghezza = immagine.getLarghezza();
		int[] pixel = new int[larghezza * immagine.getAltezza()];
		for (int y = 0; y < immagine.getAltezza(); y++) {
			for (int x = 0; x < larghezza; x++) {
				pixel[y * larghezza + (larghezza - 1 - x)] = immagine.getArgb(x, y);
			}
		}
		return new ImmagineDiPixel(larghezza, immagine.getAltezza(), pixel);
	}
}
