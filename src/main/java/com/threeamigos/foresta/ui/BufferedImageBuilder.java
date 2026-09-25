package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.Logger;

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
		if (resource != null && !resource.isEmpty()) {
			BufferedImage immagine = provaACaricare(resource);
			if (immagine == null) {
				System.exit(0);
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
				BufferedImage copy = gc.createCompatibleImage(img.getWidth(), img.getHeight(), img.getTransparency());
				Graphics2D g2d = copy.createGraphics();
				g2d.drawImage(img, 0, 0, null);
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
