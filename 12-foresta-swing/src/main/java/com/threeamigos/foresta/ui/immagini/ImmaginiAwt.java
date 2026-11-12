package com.threeamigos.foresta.ui.immagini;

import java.awt.image.BufferedImage;

/**
 * Il ponte fra le immagini di Swing e la {@link ImmagineDiPixel} che il modulo comune sa leggere.
 */
public final class ImmaginiAwt {

	private ImmaginiAwt() {
	}

	/**
	 * I pixel di un'immagine di Swing, come ARGB
	 */
	public static ImmagineDiPixel pixel(BufferedImage immagine) {
		int larghezza = immagine.getWidth();
		int altezza = immagine.getHeight();
		int[] argb = new int[larghezza * altezza];
		immagine.getRGB(0, 0, larghezza, altezza, argb, 0, larghezza);
		return new ImmagineDiPixel(larghezza, altezza, argb);
	}

	/**
	 * Un'immagine di Swing con i pixel di una {@link ImmagineDiPixel}
	 */
	public static BufferedImage buffered(ImmagineDiPixel immagine) {
		BufferedImage risultato = new BufferedImage(immagine.getLarghezza(), immagine.getAltezza(), BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < immagine.getAltezza(); y++) {
			for (int x = 0; x < immagine.getLarghezza(); x++) {
				risultato.setRGB(x, y, immagine.getArgb(x, y));
			}
		}
		return risultato;
	}

	/**
	 * L'immagine rovesciata orizzontalmente, pixel per pixel. Non è ancora compatibile con lo schermo: lo diventa
	 * quando la costruisce MappaPersonaggioImmagine (vedi BufferedImageBuilder.ingrandisci).
	 */
	/**
	 * L'immagine su fondo nero, senza più trasparenze: le tessere della mappa hanno parti trasparenti, e la mappa le vuole sul nero
	 */
	public static BufferedImage suFondoNero(BufferedImage immagine) {
		BufferedImage risultato = new BufferedImage(immagine.getWidth(), immagine.getHeight(), BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics2D g = risultato.createGraphics();
		g.setColor(java.awt.Color.BLACK);
		g.fillRect(0, 0, immagine.getWidth(), immagine.getHeight());
		g.drawImage(immagine, 0, 0, null);
		g.dispose();
		return risultato;
	}

	public static BufferedImage specchia(BufferedImage immagine) {
		BufferedImage specchiata = new BufferedImage(immagine.getWidth(), immagine.getHeight(), BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = specchiata.createGraphics();
		g.drawImage(immagine, immagine.getWidth(), 0, -immagine.getWidth(), immagine.getHeight(), null);
		g.dispose();
		return specchiata;
	}
}
