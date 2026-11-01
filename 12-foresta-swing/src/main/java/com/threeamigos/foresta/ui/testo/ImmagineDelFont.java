package com.threeamigos.foresta.ui.testo;

import java.awt.image.BufferedImage;

/**
 * Un font Doomdark scritto in un'immagine (GIF o PNG), con la griglia di {@link AtlanteDiGlifi}: i glifi sono bianchi o
 * in tonalità di grigio su nero. Il livello di un pixel è il suo grigio (se l'immagine ha un canale alfa, come un PNG,
 * il grigio per l'alfa): 0, cioè il nero puro, è trasparente, 255 è il colore pieno del testo e i grigi sfumano il
 * colore sullo sfondo. Un riquadro con un pixel di un colore che non è né rosso né una tonalità di grigio è un errore
 * dell'immagine. La larghezza di un glifo è la posizione del suo pixel acceso (livello maggiore di zero) più a destra,
 * più uno; lo spazio, che non ha pixel accesi, ha la larghezza che si dice a chi carica l'immagine.
 * <p>
 * Per i glifi a colori, come l'alfabeto grande, vedi {@link ImmagineDelFontAColori}.
 */
public final class ImmagineDelFont extends AtlanteDiGlifi<GlifoDelFont> {

	private ImmagineDelFont(Letto<GlifoDelFont> letto) {
		super(letto);
	}

	/**
	 * Legge l'immagine del font da una risorsa, per esempio {@code /com/threeamigos/foresta/img/font/doomdark_small.gif}.
	 *
	 * @param larghezzaDelloSpazio la larghezza dello spazio, che non si può misurare sui pixel
	 */
	public static ImmagineDelFont carica(String risorsa, int larghezzaDelloSpazio) {
		return daImmagine(leggiRisorsa(risorsa), larghezzaDelloSpazio);
	}

	/**
	 * Legge i glifi da un'immagine già in memoria, con la stessa griglia di carica.
	 */
	public static ImmagineDelFont daImmagine(BufferedImage immagine, int larghezzaDelloSpazio) {
		return new ImmagineDelFont(leggiLaGriglia(immagine,
				(img, carattere, x0, y0, larghezza, altezza) -> leggiRiquadro(img, carattere, x0, y0, larghezza, altezza, larghezzaDelloSpazio)));
	}

	private static GlifoDelFont leggiRiquadro(BufferedImage immagine, char carattere, int x0, int y0,
											  int larghezza, int altezza, int larghezzaDelloSpazio) {
		byte[] livelli = new byte[larghezza * altezza];
		int ultimaColonna = -1;
		for (int y = 0; y < altezza; y++) {
			for (int x = 0; x < larghezza; x++) {
				int argb = immagine.getRGB(x0 + x, y0 + y);
				int trasparenza = argb >>> 24;
				if (trasparenza == 0) {
					continue; // trasparente: il colore non conta
				}
				int rgb = argb & 0xFFFFFF;
				if (rgb == ROSSO) {
					return null;
				}
				int rosso = rgb >> 16;
				if (rosso != ((rgb >> 8) & 0xFF) || rosso != (rgb & 0xFF)) {
					throw new IllegalStateException("Pixel di colore non previsto (#" + Integer.toHexString(rgb | 0x1000000).substring(1)
							+ ") nel riquadro di '" + carattere + "' alla posizione " + x + "," + y + ": ci vogliono grigi o rosso");
				}
				// Un'immagine senza canale alfa ha trasparenza 255: il livello è il grigio
				int livello = rosso * trasparenza / 255;
				livelli[y * larghezza + x] = (byte) livello;
				if (livello > 0) {
					ultimaColonna = Math.max(ultimaColonna, x);
				}
			}
		}
		int larghezzaDelGlifo = ultimaColonna + 1;
		if (larghezzaDelGlifo == 0) {
			if (carattere != ' ') {
				return null;
			}
			return new GlifoDelFont(larghezzaDelloSpazio, altezza, new byte[larghezzaDelloSpazio * altezza]);
		}
		byte[] tagliati = new byte[larghezzaDelGlifo * altezza];
		for (int y = 0; y < altezza; y++) {
			System.arraycopy(livelli, y * larghezza, tagliati, y * larghezzaDelGlifo, larghezzaDelGlifo);
		}
		return new GlifoDelFont(larghezzaDelGlifo, altezza, tagliati);
	}
}
