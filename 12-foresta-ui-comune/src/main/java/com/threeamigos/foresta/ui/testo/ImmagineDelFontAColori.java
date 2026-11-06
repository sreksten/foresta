package com.threeamigos.foresta.ui.testo;

import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;

/**
 * Un font scritto in un'immagine (PNG) con i glifi a colori, come l'alfabeto grande (quello di
 * {@code img/font/alfabeto_grande.png}: rampicante verde su pietra grigia), con la griglia di {@link AtlanteDiGlifi}. Ogni pixel tiene
 * il suo colore: il nero puro (0, 0, 0) e i pixel trasparenti sono trasparenti, tutti gli altri sono opachi (o con
 * l'alfa dell'immagine, se ce l'ha). Un riquadro con un pixel rosso puro è un glifo che manca. La larghezza di un
 * glifo è la posizione del suo pixel acceso più a destra, più uno; lo spazio, che non ha pixel accesi, ha la larghezza
 * che si dice a chi carica l'immagine.
 * <p>
 * Per i font a livelli di grigio, che prendono il colore dal testo, vedi {@link ImmagineDelFont}.
 */
public final class ImmagineDelFontAColori extends AtlanteDiGlifi<GlifoAColori> {

	private ImmagineDelFontAColori(Letto<GlifoAColori> letto) {
		super(letto);
	}

	/**
	 * Legge l'immagine del font da una risorsa
	 *
	 * @param larghezzaDelloSpazio la larghezza dello spazio, che non si può misurare sui pixel
	 */
	public static ImmagineDelFontAColori carica(String risorsa, int larghezzaDelloSpazio) {
		return daImmagine(leggiRisorsa(risorsa), larghezzaDelloSpazio);
	}

	/**
	 * Legge i glifi da un'immagine già in memoria, con la stessa griglia di carica.
	 */
	public static ImmagineDelFontAColori daImmagine(ImmagineDiPixel immagine, int larghezzaDelloSpazio) {
		return new ImmagineDelFontAColori(leggiLaGriglia(immagine,
				(img, carattere, x0, y0, larghezza, altezza) -> leggiRiquadro(img, carattere, x0, y0, larghezza, altezza, larghezzaDelloSpazio)));
	}

	private static GlifoAColori leggiRiquadro(ImmagineDiPixel immagine, char carattere, int x0, int y0,
											  int larghezza, int altezza, int larghezzaDelloSpazio) {
		int[] pixel = new int[larghezza * altezza];
		int ultimaColonna = -1;
		for (int y = 0; y < altezza; y++) {
			for (int x = 0; x < larghezza; x++) {
				int argb = immagine.getArgb(x0 + x, y0 + y);
				int rgb = argb & 0xFFFFFF;
				if ((argb >>> 24) == 0) {
					continue; // trasparente: il colore non conta
				}
				if (rgb == ROSSO) {
					return null;
				}
				if (rgb == 0) {
					continue; // il nero puro è trasparente
				}
				pixel[y * larghezza + x] = argb;
				ultimaColonna = Math.max(ultimaColonna, x);
			}
		}
		int larghezzaDelGlifo = ultimaColonna + 1;
		if (larghezzaDelGlifo == 0) {
			if (carattere != ' ') {
				return null;
			}
			return new GlifoAColori(larghezzaDelloSpazio, altezza, new int[larghezzaDelloSpazio * altezza]);
		}
		int[] tagliati = new int[larghezzaDelGlifo * altezza];
		for (int y = 0; y < altezza; y++) {
			System.arraycopy(pixel, y * larghezza, tagliati, y * larghezzaDelGlifo, larghezzaDelGlifo);
		}
		return new GlifoAColori(larghezzaDelGlifo, altezza, tagliati);
	}
}
