package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Statistiche;

import java.awt.*;
import java.awt.image.BufferedImage;

class DisplayableCanvasRiquadroStatistiche implements Finestra {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_STATISTICHE = 6;

	private final DoomdarkFontMedium fontMedium = DoomdarkFontMedium.getInstance();
	private final DoomdarkColorModel.Color coloreTestata = DoomdarkColorModel.Color.LIGHT_GRAY;

	private final int topLeftX;
	private final int topLeftY;
	private final int moneteY;
	private final int gemmeY;
	private final int puntiY;
	private final int scrittaX;
	private final int totaleX;
	
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
		
		Image image = ImageCache.get("Monete", fontMedium, coloreTestata);
		graphics.drawImage(image, scrittaX, moneteY, null);
		image = DoomdarkTextProducer.getImage(gruppoGiocatore.getMonete(), fontMedium);
		graphics.drawImage(image, totaleX - image.getWidth(null), moneteY, null);
		
		image = ImageCache.get("Gemme", fontMedium, coloreTestata);
		graphics.drawImage(image, scrittaX, gemmeY, null);
		image = DoomdarkTextProducer.getImage(gruppoGiocatore.getPreziosi(), fontMedium);
		graphics.drawImage(image, totaleX - image.getWidth(null), gemmeY, null);

		image = ImageCache.get("Punti", fontMedium, coloreTestata);
		graphics.drawImage(image, scrittaX, puntiY, null);
		int puntiEsperienza = Statistiche.getPuntiEsperienza();
		int puntiPerProssimoLivello = Statistiche.getPuntiEsperienzaPerProssimoLivello();
		image = DoomdarkTextProducer.getImage(puntiEsperienza + "/" + puntiPerProssimoLivello, fontMedium);
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
