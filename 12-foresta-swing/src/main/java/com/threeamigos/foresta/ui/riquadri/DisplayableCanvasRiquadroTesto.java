package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkTextRectangle2x;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Il riquadro di testo in cui scorrono le frasi che raccontano la partita.
 */
public class DisplayableCanvasRiquadroTesto extends RiquadroTesto {

	private final DoomdarkTextRectangle2x doomdarkTextRectangle;
	// L'immagine finita del riquadro (ombra del drago + testo sfumato), rifatta solo
	// quando cambia la versione del testo
	private BufferedImage immagineTesto;
	private int versioneDisegnata;

	public DisplayableCanvasRiquadroTesto(int topLeftX, int topLeftY, int width, int height) {
		super(topLeftX, topLeftY, width, height);
		doomdarkTextRectangle = new DoomdarkTextRectangle2x(getStorico());
	}

	/**
	 * Disegna le ultime frasi che entrano nel riquadro.
	 */
	public void disegnaTesto(Graphics2D graphics) {
		sincronizzaScorrimento();
		int versione = doomdarkTextRectangle.getVersione();
		if (immagineTesto == null || versione != versioneDisegnata) {
			Image raster = Toolkit.getDefaultToolkit().createImage(doomdarkTextRectangle.getImageSource());
			immagineTesto = componi(raster);
			versioneDisegnata = versione;
		}
		graphics.drawImage(immagineTesto, getTopLeftX(), getTopLeftY(), null);

		final int SPACING = ImageCache.SPACING;
		if (doomdarkTextRectangle.isScorribileSu()) {
			Image frecciaSu = ImageCache.componenteScorrevoleFrecciaSu;
			graphics.drawImage(frecciaSu, getTopLeftX() + getWidth() - frecciaSu.getWidth(null) - SPACING, getTopLeftY() + SPACING, null);
		}
		if (doomdarkTextRectangle.isScorribileGiu()) {
			Image frecciaGiu = ImageCache.componenteScorrevoleFrecciaGiu;
			graphics.drawImage(frecciaGiu, getTopLeftX() + getWidth() - frecciaGiu.getWidth(null) - SPACING,
					getTopLeftY() + getHeight() - frecciaGiu.getHeight(null) - SPACING, null);
		}
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
		// La sfumatura dall'alto: nera in cima, bianca piena alla sua fine (vedi RiquadroTesto.getAltezzaDellaSfumatura)
		int vertLimit = Math.min(imageHeight, getAltezzaDellaSfumatura());
		for (int y = 0; y < vertLimit; y++) {
			int livello = Math.round(luminositaAllaQuota(y) * 255);
			int mask = (livello << 16) + (livello << 8) + livello;
			for (int x = 0; x < imageWidth; x++) {
				int argb = copy.getRGB(x, y);
				int alfa = argb >>> 24;
				if (alfa != 0 && (argb & 0x00FFFFFF) != 0) {
					// La sfumatura dall'alto scurisce il colore e lascia l'alfa del glifo, così i bordi restano sfumati
					copy.setRGB(x, y, (alfa << 24) | (argb & mask));
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
