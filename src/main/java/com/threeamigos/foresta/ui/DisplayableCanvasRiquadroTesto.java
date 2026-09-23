package com.threeamigos.foresta.ui;

import java.awt.*;
import java.awt.image.BufferedImage;

class DisplayableCanvasRiquadroTesto implements Finestra {

	// Righe di testo per ogni scatto della rotella
	private static final int PASSO_SCORRIMENTO = 1;

	private final int topLeftX;
	private final int topLeftY;
	private final DoomdarkTextRectangle2x doomdarkTextRectangle;
	// L'immagine finita del riquadro (ombra del drago + testo sfumato), rifatta solo
	// quando cambia la versione del testo
	private BufferedImage immagineTesto;
	private int versioneDisegnata;

	DisplayableCanvasRiquadroTesto(int topLeftX, int topLeftY, int width, int height) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		doomdarkTextRectangle = new DoomdarkTextRectangle2x(width, height);
	}

	void clear() {
		doomdarkTextRectangle.clear();
	}
	
	void addString(String messaggio) {
		doomdarkTextRectangle.addString(messaggio);
	}


	@Override
	public void processaRotella(int x, int y, int numeroRotazioni, MovimentoRotella movimentoRotella) {
		if (movimentoRotella == MovimentoRotella.SU) {
			doomdarkTextRectangle.scorri(numeroRotazioni * PASSO_SCORRIMENTO);
		} else {
			doomdarkTextRectangle.scorri(-numeroRotazioni * PASSO_SCORRIMENTO);
		}
	}

	void disegnaTesto(Graphics2D graphics) {
		int versione = doomdarkTextRectangle.getVersione();
		if (immagineTesto == null || versione != versioneDisegnata) {
			Image raster = Toolkit.getDefaultToolkit().createImage(doomdarkTextRectangle.getImageSource());
			immagineTesto = componi(raster);
			versioneDisegnata = versione;
		}
		graphics.drawImage(immagineTesto, topLeftX, topLeftY, null);
	}

	/**
	 * Compone il raster del testo sull'ombra del drago, schiarendo le righe dall'alto
	 * verso il basso così che il testo più vecchio sfumi.
	 */
	private BufferedImage componi(Image image) {

		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);

		final GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

		BufferedImage background = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), Transparency.OPAQUE);
		Graphics2D backgroundG2d = background.createGraphics();
		float transparency = (float)0.3;
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency);
		backgroundG2d.setComposite(ac);
		backgroundG2d.drawImage(ImageCache.ombraDelDrago, imageWidth - ImageCache.ombraDelDrago.getWidth() >> 1, imageHeight - ImageCache.ombraDelDrago.getHeight() >> 1, null);

		backgroundG2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

		BufferedImage copy = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), Transparency.TRANSLUCENT);
		Graphics2D g2d = copy.createGraphics();		
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();
		int vertLimit = Math.min(imageHeight, 256);
		for (int y = 0; y < vertLimit; y++) {
			int mask = (y << 16) + (y << 8) + y;
			for (int x = 0; x < imageWidth; x++) {
				int argb = copy.getRGB(x, y);
				if ((argb & 0x00FFFFFF) != 0) {
					copy.setRGB(x, y, 0xFF000000 | (copy.getRGB(x, y) & mask));
				} else {
					copy.setRGB(x, y, 0x00000000);
				}
			}
		}

		backgroundG2d.drawImage(copy, 0, 0, null);
		backgroundG2d.dispose();

		return background;
	}

}
