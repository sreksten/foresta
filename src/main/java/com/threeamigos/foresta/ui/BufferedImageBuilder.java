package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.strumenti.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class BufferedImageBuilder {
	
	private BufferedImageBuilder() {
	}

	private static final GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

	/**
	 * Carica una risorsa grafica all'avvio: un'immagine mancante è un errore fatale.
	 */
	public static BufferedImage buildBufferedImage(String resource) {
		return buildBufferedImage(resource, 1.0);
	}

	/**
	 * Come {@link #buildBufferedImage(String)}, ma l'immagine viene ingrandita di {@code zoom} (2.0 = 200%).
	 */
	public static BufferedImage buildBufferedImage(String resource, double zoom) {
		if (resource != null && !resource.isEmpty()) {
			BufferedImage immagine = provaACaricare(resource, zoom);
			if (immagine == null) {
				// Codice d'uscita diverso da 0, perché chi lancia il gioco da uno script veda l'errore
				Logger.log("Risorsa grafica di base mancante o illeggibile: " + resource);
				System.exit(1);
			}
			return immagine;
		}
		return null;
	}

	/**
	 * Carica una risorsa grafica senza fermare il gioco se manca o non è leggibile:
	 * l'errore viene registrato e si ottiene null. Adatto alle risorse caricate al volo,
	 * come quelle degli intermezzi.
	 */
	public static BufferedImage provaACaricare(String resource) {
		return provaACaricare(resource, 1.0);
	}

	/**
	 * Come {@link #provaACaricare(String)}, ma l'immagine viene ingrandita di {@code zoom} (2.0 = 200%):
	 * i pixel vengono solo replicati, senza interpolazione. Con zoom 1.0 l'immagine resta com'è.
	 */
	public static BufferedImage provaACaricare(String resource, double zoom) {
		if (!(zoom > 0)) {
			throw new IllegalArgumentException("Zoom non valido: " + zoom);
		}
		if (resource != null && !resource.isEmpty()) {
			// ImageIO.read(InputStream) non chiude lo stream: lo chiude il try
			try (InputStream in = BufferedImageBuilder.class.getResourceAsStream("/com/threeamigos/foresta/img/" + resource)) {
				if (in == null) {
					throw new IllegalArgumentException("Non trovo il file " + resource);
				}
				BufferedImage img = ImageIO.read(in);
				if (img == null) {
					throw new IllegalArgumentException("Formato non riconosciuto per il file " + resource);
				}
				int larghezza = (int) Math.round(img.getWidth() * zoom);
				int altezza = (int) Math.round(img.getHeight() * zoom);
				BufferedImage copy = gc.createCompatibleImage(larghezza, altezza, img.getTransparency());
				Graphics2D g2d = copy.createGraphics();
				if (zoom != 1.0) {
					g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				}
				g2d.drawImage(img, 0, 0, larghezza, altezza, null);
				g2d.dispose();
				Logger.log("Image resource: " + resource + ", " + copy.getWidth() + "x" + copy.getHeight());
				return copy;
			} catch (Exception e) {
				Logger.log(e);
			}
		}
		return null;
	}

}
