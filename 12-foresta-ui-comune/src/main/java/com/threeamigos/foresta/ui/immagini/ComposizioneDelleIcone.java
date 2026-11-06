package com.threeamigos.foresta.ui.immagini;

/**
 * Come si compone un'icona della barra dei comandi, in pixel: lo sfondo, la sagoma nera del disegno spostata di un pixel in basso a destra (l'ombra) e il
 * disegno. Lo stesso per Swing e libGDX.
 */
public final class ComposizioneDelleIcone {

	private static final int NERO = 0xFF000000;

	private ComposizioneDelleIcone() {
	}

	/**
	 * L'icona come si disegna nel pannello, a metà risoluzione e prima dello zoom: lo sfondo del colore voluto, la sagoma nera del disegno spostata di un pixel
	 * in basso e a destra (l'ombra) e poi il disegno. Il disegno sta in alto a sinistra; la dimensione è quella dello sfondo. Dove il disegno è trasparente non
	 * c'è ombra.
	 */
	public static ImmagineDiPixel componi(ImmagineDiPixel sfondo, ImmagineDiPixel disegno) {
		int larghezza = sfondo.getLarghezza();
		int altezza = sfondo.getAltezza();
		int[] pixel = new int[larghezza * altezza];
		for (int y = 0; y < altezza; y++) {
			for (int x = 0; x < larghezza; x++) {
				pixel[y * larghezza + x] = sfondo.getArgb(x, y);
			}
		}
		for (int y = 0; y < disegno.getAltezza(); y++) {
			for (int x = 0; x < disegno.getLarghezza(); x++) {
				boolean ombraDentro = x + 1 < larghezza && y + 1 < altezza;
				if (ombraDentro && (disegno.getArgb(x, y) >>> 24) != 0) {
					pixel[(y + 1) * larghezza + x + 1] = NERO;
				}
			}
		}
		// Il disegno si mette sopra (con la sua trasparenza), non lo sostituisce
		for (int y = 0; y < Math.min(altezza, disegno.getAltezza()); y++) {
			for (int x = 0; x < Math.min(larghezza, disegno.getLarghezza()); x++) {
				int argb = disegno.getArgb(x, y);
				if ((argb >>> 24) != 0) {
					pixel[y * larghezza + x] = com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande.sopra(argb, pixel[y * larghezza + x]);
				}
			}
		}
		return new ImmagineDiPixel(larghezza, altezza, pixel);
	}
}
