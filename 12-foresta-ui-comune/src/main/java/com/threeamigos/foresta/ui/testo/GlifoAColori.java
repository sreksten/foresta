package com.threeamigos.foresta.ui.testo;

/**
 * Un carattere di un font a colori (vedi {@link ImmagineDelFontAColori}): una piccola immagine, alta come un riquadro e
 * larga fino al suo pixel acceso più a destra. Ogni pixel ha il suo colore, in ARGB non premoltiplicato: il pixel
 * trasparente è 0.
 */
public final class GlifoAColori implements GlifoDiAtlante {

	private final int larghezza;
	private final int altezza;
	private final int[] pixel;

	/**
	 * @param pixel larghezza x altezza pixel ARGB, per righe dall'alto
	 */
	GlifoAColori(int larghezza, int altezza, int[] pixel) {
		if (pixel.length != larghezza * altezza) {
			throw new IllegalArgumentException("Servono " + larghezza * altezza + " pixel, ce ne sono " + pixel.length);
		}
		this.larghezza = larghezza;
		this.altezza = altezza;
		this.pixel = pixel;
	}

	@Override
	public int getLarghezza() {
		return larghezza;
	}

	@Override
	public int getAltezza() {
		return altezza;
	}

	/**
	 * Il colore del pixel in ARGB, 0 se è trasparente
	 */
	public int getPixel(int x, int y) {
		return pixel[y * larghezza + x];
	}
}
