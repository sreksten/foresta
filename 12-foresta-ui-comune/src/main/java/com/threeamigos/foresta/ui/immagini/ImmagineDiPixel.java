package com.threeamigos.foresta.ui.immagini;

/**
 * Un'immagine come semplice griglia di pixel ARGB (alfa nel byte alto), senza nessun toolkit grafico: quel che le interfacce
 * grafiche si scambiano quando devono leggere i pixel di un'immagine del gioco (la griglia dei font, per esempio) e non solo
 * disegnarla. Si legge da un PNG con {@link LetturaPng}.
 */
public final class ImmagineDiPixel {

	private final int larghezza;
	private final int altezza;
	private final int[] argb;

	/**
	 * @param argb i pixel per righe, dall'alto in basso; la lunghezza è larghezza × altezza
	 */
	public ImmagineDiPixel(int larghezza, int altezza, int[] argb) {
		if (larghezza < 0 || altezza < 0 || argb.length != larghezza * altezza) {
			throw new IllegalArgumentException("Pixel " + argb.length + " per un'immagine " + larghezza + "x" + altezza);
		}
		this.larghezza = larghezza;
		this.altezza = altezza;
		this.argb = argb;
	}

	public int getLarghezza() {
		return larghezza;
	}

	public int getAltezza() {
		return altezza;
	}

	/**
	 * Il pixel in quel punto, come 0xAARRGGBB
	 */
	public int getArgb(int x, int y) {
		return argb[y * larghezza + x];
	}

	/**
	 * Una copia della porzione dell'immagine in quel rettangolo
	 */
	public ImmagineDiPixel ritaglia(int x, int y, int larghezzaRitaglio, int altezzaRitaglio) {
		if (x < 0 || y < 0 || x + larghezzaRitaglio > larghezza || y + altezzaRitaglio > altezza) {
			throw new IllegalArgumentException("Il rettangolo " + x + "," + y + " " + larghezzaRitaglio + "x" + altezzaRitaglio
					+ " esce dall'immagine " + larghezza + "x" + altezza);
		}
		int[] pixel = new int[larghezzaRitaglio * altezzaRitaglio];
		for (int riga = 0; riga < altezzaRitaglio; riga++) {
			System.arraycopy(argb, (y + riga) * larghezza + x, pixel, riga * larghezzaRitaglio, larghezzaRitaglio);
		}
		return new ImmagineDiPixel(larghezzaRitaglio, altezzaRitaglio, pixel);
	}
}
