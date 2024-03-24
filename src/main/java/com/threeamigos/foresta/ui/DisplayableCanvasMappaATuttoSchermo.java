package com.threeamigos.foresta.ui;

import java.awt.Graphics2D;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;

class DisplayableCanvasMappaATuttoSchermo {

	private int width;
	private int height;
	private int mappaXOffset;
	private int mappaYOffset;

	DisplayableCanvasMappaATuttoSchermo(int width, int height) {
		this.width = width;
		this.height = height;
	}

	void centraMappa() {
		Logger.log("Centro la mappa");
		mappaXOffset = GruppoGiocatore.getIstanza().getX();
		mappaYOffset = GruppoGiocatore.getIstanza().getY();
		InfoMappa infoMappa = new InfoMappa();
		mappaXOffset = (infoMappa.daX + infoMappa.aX) / 2;
		mappaYOffset = (infoMappa.daY + infoMappa.aY) / 2;
	}

	void muoviMappa(Comando direzione) {
		InfoMappa infoMappa = new InfoMappa();
		if (direzione == Comando.SINISTRA) {
			Logger.log("Xoffset: " + mappaXOffset);
			if (infoMappa.daX > Foresta.getMinXConosciuta()) {
				mappaXOffset--;
				Logger.log("infoMappa.daX = " + infoMappa.daX + ", mappa.getMinXConosciuta = " + Foresta.getMinXConosciuta() + ", posso andare a ovest. Nuovo Xoffset = " + mappaXOffset);
			} else {
				Logger.log("infoMappa.daX = " + infoMappa.daX + ", mappa.getMinXConosciuta = " + Foresta.getMinXConosciuta() + ", NON posso andare a ovest");
			}
		} else if (direzione == Comando.SU) {
			Logger.log("Yoffset: " + mappaYOffset);
			if (infoMappa.daY > Foresta.getMinYConosciuta()) {
				mappaYOffset--;
				Logger.log("infoMappa.daY = " + infoMappa.daY + ", mappa.getMinYConosciuta = " + Foresta.getMinYConosciuta() + ", posso andare a nord. Nuovo Yoffset = " + mappaYOffset);
			} else {
				Logger.log("infoMappa.daY = " + infoMappa.daY + ", mappa.getMinYConosciuta = " + Foresta.getMinYConosciuta() + ", NON posso andare a nord");
			}
		} else if (direzione == Comando.GIU) {
			Logger.log("Yoffset: " + mappaYOffset);
			if (infoMappa.aY < Foresta.getMaxYConosciuta()) {
				mappaYOffset++;
				Logger.log("infoMappa.aY = " + infoMappa.aY + ", mappa.getMaxYConosciuta = " + Foresta.getMaxYConosciuta() + ", posso andare a sud. Nuovo Yoffset = " + mappaYOffset);
			} else {
				Logger.log("infoMappa.aY = " + infoMappa.aY + ", mappa.getMaxYConosciuta = " + Foresta.getMaxYConosciuta() + ", NON posso andare a sud");
			}
		} else if (direzione == Comando.DESTRA) {
			Logger.log("Xoffset: " + mappaXOffset);
			if (infoMappa.aX < Foresta.getMaxXConosciuta()) {
				mappaXOffset++;
				Logger.log("infoMappa.aX = " + infoMappa.aX + ", mappa.getMaxXConosciuta = " + Foresta.getMaxXConosciuta() + ", posso andare a est. Nuovo Xoffset = " + mappaXOffset);
			} else {
				Logger.log("infoMappa.aX = " + infoMappa.aX + ", mappa.getMaxXConosciuta = " + Foresta.getMaxXConosciuta() + ", NON posso andare a est");
			}
		}
	}
	
	void disegnaMappaATuttoSchermo(Graphics2D graphics) {
		CoordinateMD coordinateGruppo = GruppoGiocatore.getIstanza().getCoordinate(); 
		InfoMappa infoMappa = new InfoMappa();

		int coordinataSchermoX = (width - (infoMappa.aX - infoMappa.daX) * infoMappa.larghezzaIcona) >> 1;
		int coordinataInizialeY = (height - (infoMappa.aY - infoMappa.daY) * infoMappa.altezzaIcona) >> 1;
		
		for (int x = infoMappa.daX; x <= infoMappa.aX; x++) {
			int coordinataSchermoY = coordinataInizialeY;
			for (int y = infoMappa.daY; y <= infoMappa.aY; y++) {
				CoordinateMD coordinateCorrenti = new CoordinateMD(x, y);
				if (coordinateCorrenti.equals(coordinateGruppo)) {
					graphics.drawImage(ImageCache.segnalino, coordinataSchermoX, coordinataSchermoY, null);
				} else if (Foresta.isLocazioneConosciuta(coordinateCorrenti)) {
					ClassiLocazione classeLocazione = Foresta.getLocazione(coordinateCorrenti);
					graphics.drawImage(ImageCache.mappa.get(classeLocazione), coordinataSchermoX, coordinataSchermoY, null);
				}
				coordinataSchermoY += infoMappa.altezzaIcona;
			}
			coordinataSchermoX += infoMappa.larghezzaIcona;
		}
	}

	private class InfoMappa {

		private int larghezzaIcona;
		private int altezzaIcona;
		private int quanteLocazioniLungoX;
		private int quanteLocazioniLungoY;
		private int daX;
		private int aX;
		private int daY;
		private int aY;
		
		InfoMappa() {
			
			larghezzaIcona = ImageCache.mappa.get(ClassiLocazione.BOSCO).getWidth();
			altezzaIcona = ImageCache.mappa.get(ClassiLocazione.BOSCO).getHeight();

			quanteLocazioniLungoX = width / larghezzaIcona;
			if (quanteLocazioniLungoX >= Foresta.getDimensioneX()) {
				quanteLocazioniLungoX = Foresta.getDimensioneX();
			}

			quanteLocazioniLungoY = height / altezzaIcona;
			if (quanteLocazioniLungoY >= Foresta.getDimensioneY()) {
				quanteLocazioniLungoY = Foresta.getDimensioneY();
			}

			if (quanteLocazioniLungoX >= Foresta.getDimensioneX()) {
				daX = Foresta.getMinXConosciuta();
				aX = Foresta.getMaxXConosciuta();
			} else {
				daX = mappaXOffset - quanteLocazioniLungoX / 2;
				if (daX < 0) {
					daX = 0;
					aY = quanteLocazioniLungoY - 1;
				} else {
					aX = daX + quanteLocazioniLungoX - 1;
					if (aX >= Foresta.getDimensioneX()) {
						aX = Foresta.getDimensioneX() - 1;
						daX = aX - quanteLocazioniLungoX + 1; 				
					}
				}
			}
			if (quanteLocazioniLungoY >= Foresta.getDimensioneY()) {
				daY = Foresta.getMinYConosciuta();
				aY = Foresta.getMaxYConosciuta();
			} else {
				daY = mappaYOffset - (quanteLocazioniLungoY / 2);
				if (daY < 0) {
					daY = 0;
					aY = quanteLocazioniLungoY - 1;
				} else {
					aY = daY + quanteLocazioniLungoY - 1;
					if (aY >= Foresta.getDimensioneY()) {
						aY = Foresta.getDimensioneY() - 1;
						daY = aY - quanteLocazioniLungoY + 1;
					}
				}
			}
		}
	}
}
