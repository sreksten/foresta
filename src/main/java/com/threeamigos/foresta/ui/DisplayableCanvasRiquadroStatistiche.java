package com.threeamigos.foresta.ui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;

class DisplayableCanvasRiquadroStatistiche {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_STATISTICHE = 6;

	private final DoomdarkFontMedium fontMedium = DoomdarkFontMedium.getInstance();

	private int topLeftX;
	private int topLeftY;
	private int moneteY;
	private int gemmeY;
	private int puntiY;
	private int scrittaX;
	private int totaleX;
	
	DisplayableCanvasRiquadroStatistiche(int topLeftX, int topLeftY) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		moneteY = topLeftY + DIMENSIONE_BORDO_INTERNO_CORNICE_STATISTICHE + 4;
		gemmeY = moneteY + fontMedium.getHeight() + 1;
		puntiY = gemmeY + fontMedium.getHeight() + 1;
		scrittaX = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_STATISTICHE + 4;
		totaleX = topLeftX + ImageCache.cornicePiccola.getWidth() - DIMENSIONE_BORDO_INTERNO_CORNICE_STATISTICHE - 4;
		
	}
	
	void disegnaStatistiche(Graphics2D graphics) {
		GruppoGiocatore gruppoGiocatore = GruppoGiocatore.getIstanza();
		graphics.drawImage(ImageCache.cornicePiccola, topLeftX, topLeftY, null);
		
		Image image = DoomdarkTextProducer.getImage("Monete", fontMedium);
		graphics.drawImage(image, scrittaX, moneteY, null);
		image = DoomdarkTextProducer.getImage(gruppoGiocatore.getMonete(), fontMedium);
		graphics.drawImage(image, totaleX - image.getWidth(null), moneteY, null);
		
		image = DoomdarkTextProducer.getImage("Gemme", fontMedium);
		graphics.drawImage(image, scrittaX, gemmeY, null);
		image = DoomdarkTextProducer.getImage(gruppoGiocatore.getPreziosi(), fontMedium);
		graphics.drawImage(image, totaleX - image.getWidth(null), gemmeY, null);

		image = DoomdarkTextProducer.getImage("Punti", fontMedium);
		graphics.drawImage(image, scrittaX, puntiY, null);
		image = DoomdarkTextProducer.getImage(Statistiche.getPunti(), fontMedium);
		graphics.drawImage(image, totaleX - image.getWidth(null), puntiY, null);
	}

	SpriteATempo variaMonete(int variazione) {
		if (variazione == 0) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteMoneta;
		return new SpriteATempo(icona, variazione, fontMedium, totaleX, moneteY);
	}
	
	SpriteATempo variaGemme(int variazione) {
		if (variazione == 0) {
			return null;
		}
		BufferedImage icona = ImageCache.spriteGemma;
		return new SpriteATempo(icona, variazione, fontMedium, totaleX, gemmeY);
	}
	
	SpriteATempo variaPunti(int variazione) {
		if (variazione == 0) {
			return null;
		}
		return new SpriteATempo(null, variazione, fontMedium, totaleX, puntiY);
	}
}
