package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.Comando;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.ui.sfx.CloudGenerator;
import com.threeamigos.foresta.ui.sfx.CloudInstance;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

class DisplayableCanvasMappaATuttoSchermo implements Finestra {

	private final int width;
	private final int height;
	private int mappaXOffset;
	private int mappaYOffset;

	// SFX
	private Rectangle clipRiquadro;
	private final java.util.List<CloudInstance> clouds = new ArrayList<>();

	DisplayableCanvasMappaATuttoSchermo(int width, int height) {
		this.width = width;
		this.height = height;
	}

	void creaNuvolette() {
		Random rand = new Random();

		// Calcola il rettangolo clip per le nuvole basandosi sulla mappa
		InfoMappa infoMappa = new InfoMappa();

		int coordinataSchermoX = (width - (infoMappa.aX - infoMappa.daX + 1) * infoMappa.larghezzaIcona) >> 1;
		int coordinataInizialeY = (height - (infoMappa.aY - infoMappa.daY + 1) * infoMappa.altezzaIcona) >> 1;
		int larghezzaMappa = (infoMappa.aX - infoMappa.daX + 1) * infoMappa.larghezzaIcona;
		int altezzaMappa = (infoMappa.aY - infoMappa.daY + 1) * infoMappa.altezzaIcona;

		clipRiquadro = new Rectangle(coordinataSchermoX, coordinataInizialeY, larghezzaMappa, altezzaMappa);

		// Popola la lista di nuvole prima dell'avvio dell'animazione
		for (int i = 0; i < 6; i++) {
			// Dimensione della nuvola basata sulla larghezza del rettangolo (clipRiquadro.width)
			int cloudWidth = (int)(clipRiquadro.width * 0.3) + rand.nextInt((int)(clipRiquadro.width * 0.3));
			int cloudHeight = cloudWidth / 2;

			// Genera l'immagine procedurale usando il CloudGenerator
			BufferedImage singleCloudPattern = CloudGenerator.generateCloud(cloudWidth, cloudHeight);

			// Posizione iniziale X e Y calcolate dentro i confini del rettangolo
			float startX = clipRiquadro.x + rand.nextInt(clipRiquadro.width) - cloudWidth;
			float startY = clipRiquadro.y + rand.nextInt(clipRiquadro.height - cloudHeight);

			// Effetto parallasse: le nuvole più grandi sono più "vicine" e vanno più veloci
			float speed = 0.2f + ((float) cloudWidth / clipRiquadro.width) * 0.8f;

			// Passiamo il rettangolo direttamente al costruttore della nuvola
			clouds.add(new CloudInstance(singleCloudPattern, startX, startY, speed, clipRiquadro));
		}
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

		if (clipRiquadro == null) {
			creaNuvolette();
		}

		CoordinateMD coordinateGruppo = GruppoGiocatore.getIstanza().getCoordinate();
		InfoMappa infoMappa = new InfoMappa();

		int coordinataSchermoX = (width - (infoMappa.aX - infoMappa.daX + 1) * infoMappa.larghezzaIcona) >> 1;
		int coordinataInizialeY = (height - (infoMappa.aY - infoMappa.daY + 1) * infoMappa.altezzaIcona) >> 1;

		for (int x = infoMappa.daX; x <= infoMappa.aX; x++) {
			int coordinataSchermoY = coordinataInizialeY;
			for (int y = infoMappa.daY; y <= infoMappa.aY; y++) {
				CoordinateMD coordinateCorrenti = new CoordinateMD(x, y);
				if (coordinateCorrenti.equals(coordinateGruppo)) {
					if ((System.currentTimeMillis() / 1000) % 2 == 0) {
						graphics.drawImage(ImageCache.segnalino, coordinataSchermoX, coordinataSchermoY, null);
					}
				} else if (Foresta.isLocazioneConosciuta(coordinateCorrenti)) {
					ClassiLocazione classeLocazione = Foresta.getLocazione(coordinateCorrenti);
					graphics.drawImage(ImageCache.mappa.get(classeLocazione), coordinataSchermoX, coordinataSchermoY, null);
					if (Foresta.isLocazioneVisitata(new CoordinateMD(x, y))) {
						scurisci(graphics, coordinataSchermoX, coordinataSchermoY, infoMappa.larghezzaIcona, infoMappa.altezzaIcona, 50);
					}
				}
				coordinataSchermoY += infoMappa.altezzaIcona;
			}
			coordinataSchermoX += infoMappa.larghezzaIcona;
		}

		// (Opzionale) Per vedere visivamente dove finisce il riquadro
		//graphics.setColor(Color.RED); graphics.drawRect(clipRiquadro.x, clipRiquadro.y, clipRiquadro.width, clipRiquadro.height);

		// Salva lo stato originale della Clip e del Composite
		Shape originalClip = graphics.getClip();
		Composite originalComposite = graphics.getComposite();

		// Applica la clip sulla mappa
		int mappaWidth = (infoMappa.aX - infoMappa.daX + 1) * infoMappa.larghezzaIcona;
		int mappaHeight = (infoMappa.aY - infoMappa.daY + 1) * infoMappa.altezzaIcona;
		int mappaStartX = (width - mappaWidth) >> 1;
		int mappaStartY = (height - mappaHeight) >> 1;
		graphics.clipRect(mappaStartX, mappaStartY, mappaWidth, mappaHeight);

		// Imposta la trasparenza e disegna le nuvole
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));

		for (CloudInstance cloud : clouds) {
			graphics.drawImage(cloud.getImage(), cloud.getX(), cloud.getY(), null);
		}

		// Ripristina la clip e il composite originali
		graphics.setComposite(originalComposite);
		graphics.setClip(originalClip);

		// Aggiorna la posizione delle nuvolette
		for (CloudInstance cloud : clouds) {
			cloud.update();
		}
	}

	private void scurisci(Graphics2D g, int x, int y, int width, int height, int percentualeOscuramento) {
		// Calcola alpha (0 = trasparente, 255 = nero opaco)
		int alpha = (int) (percentualeOscuramento * 2.55f);
		// Imposta il colore nero con la trasparenza calcolata
		g.setColor(new java.awt.Color(0, 0, 0, alpha));
		// Disegna il rettangolo sopra l'immagine
		g.fillRect(x, y, width, height);
	}

	private class InfoMappa {

		private final int larghezzaIcona;
		private final int altezzaIcona;
        private int daX;
		private int aX;
		private int daY;
		private int aY;
		
		InfoMappa() {
			
			larghezzaIcona = ImageCache.mappa.get(ClassiLocazione.BOSCO).getWidth();
			altezzaIcona = ImageCache.mappa.get(ClassiLocazione.BOSCO).getHeight();

            int quanteLocazioniLungoX = width / larghezzaIcona;
			if (quanteLocazioniLungoX >= Foresta.getDimensioneX()) {
				quanteLocazioniLungoX = Foresta.getDimensioneX();
			}

            int quanteLocazioniLungoY = height / altezzaIcona;
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
