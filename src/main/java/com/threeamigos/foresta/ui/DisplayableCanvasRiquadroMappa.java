package com.threeamigos.foresta.ui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;

class DisplayableCanvasRiquadroMappa {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_MAPPA = 16;

	private int topLeftX;
	private int topLeftY;

	DisplayableCanvasRiquadroMappa(int topLeftX, int topLeftY) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
	}
	
	void disegnaMappa(Graphics2D graphics) {
		GruppoGiocatore gruppoGiocatore = GruppoGiocatore.getIstanza();
		graphics.drawImage(ImageCache.corniceMappa, topLeftX, topLeftY, null);
		int mw = ImageCache.mappa.get(ClassiLocazione.BOSCO).getWidth();
		int mh = ImageCache.mappa.get(ClassiLocazione.BOSCO).getHeight();
		int gruppoX = gruppoGiocatore.getX();
		int gruppoY = gruppoGiocatore.getY();
		int daX = gruppoX - 3;
		int aX = gruppoX + 3;
		if (daX < 0) {
			daX = 0;
			aX = 6;
		}
		if (aX >= Foresta.getDimensioneX()) {
			daX = Foresta.getDimensioneX() - 7;
			aX = Foresta.getDimensioneX() - 1;
		}
		int daY = gruppoY - 3;
		int aY = gruppoY + 3;
		if (daY < 0) {
			daY = 0;
			aY = 6;
		}
		if (aY >= Foresta.getDimensioneY()) {
			daY = Foresta.getDimensioneY() - 7;
			aY = Foresta.getDimensioneY() - 1;
		}
		ClassiLocazione classeLocazione;
		int localXOffset = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_MAPPA;
		int localYOffset = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_MAPPA;
		for (int x = daX; x <= aX; x++) {
			for (int y = daY; y <= aY; y++) {
				classeLocazione = Foresta.getLocazione(x, y);
				BufferedImage image = null;
				if (x == gruppoX && y == gruppoY) {
					image = ImageCache.segnalino;
				} else {
					image = ImageCache.mappa.get(classeLocazione);
				}
				graphics.drawImage(image, localXOffset + (x - daX) * mw, localYOffset + (y - daY) * mh, null);
			}
		}
	}
	
	SpriteInterface variaMappa() {
		BufferedImage icona = ImageCache.spriteMappa;
		int x = topLeftX + ((ImageCache.corniceMappa.getWidth() - ImageCache.spriteMappa.getWidth()) >> 1);
		int y = topLeftY + ((ImageCache.corniceMappa.getHeight() - ImageCache.spriteMappa.getHeight()) >> 1);
		return new SpriteATempo(icona, x, y);
	}
}
