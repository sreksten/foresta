package com.threeamigos.foresta.ui.immagini;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Iterator;

/**
 * Le dimensioni di un'immagine tra le risorse del gioco, lette dalla sola intestazione del file senza decodificarla:
 * servono a dimensionare la finestra prima di caricare le immagini (vedi ForestaUI e il logo iniziale).
 */
public final class DimensioniRisorsa {

	private DimensioniRisorsa() {
	}

	/**
	 * Le dimensioni della risorsa dopo lo zoom con cui ImageCache la carica.
	 */
	public static Dimension di(String risorsa, double zoom) {
		Dimension d = di(risorsa);
		return new Dimension((int) Math.round(d.width * zoom), (int) Math.round(d.height * zoom));
	}

	/**
	 * Le dimensioni della risorsa, lette dall'intestazione del file.
	 *
	 * @throws IllegalArgumentException se la risorsa non c'è o il suo formato non è riconosciuto
	 */
	public static Dimension di(String risorsa) {
		String percorso = "/com/threeamigos/foresta/img/" + risorsa;
		try (InputStream in = DimensioniRisorsa.class.getResourceAsStream(percorso)) {
			if (in == null) {
				throw new IllegalArgumentException("Non trovo il file " + risorsa);
			}
			try (ImageInputStream immagine = ImageIO.createImageInputStream(in)) {
				Iterator<ImageReader> lettori = ImageIO.getImageReaders(immagine);
				if (!lettori.hasNext()) {
					throw new IllegalArgumentException("Formato non riconosciuto: " + risorsa);
				}
				ImageReader lettore = lettori.next();
				try {
					lettore.setInput(immagine, true, true);
					return new Dimension(lettore.getWidth(0), lettore.getHeight(0));
				} finally {
					lettore.dispose();
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
