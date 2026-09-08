package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.locazioni.ClassiLocazione;
import com.threeamigos.foresta.motore.Foresta;
import com.threeamigos.foresta.motore.GruppoGiocatore;
import com.threeamigos.foresta.motore.modellodati.CoordinateMD;
import com.threeamigos.foresta.ui.sfx.CloudManager;

import java.awt.*;
import java.awt.image.BufferedImage;

class DisplayableCanvasRiquadroMappa implements Finestra {

	private static final int DIMENSIONE_BORDO_INTERNO_CORNICE_MAPPA = 12;

	private final int topLeftX;
	private final int topLeftY;
	private final int larghezzaSchermo;
	private final int altezzaSchermo;

	private final int minOffsetPerNuvole;
    private final int larghezzaRiquadroMappa;

	DisplayableCanvasRiquadroMappa(int topLeftX, int topLeftY, int larghezzaSchermo, int altezzaSchermo) {
		this.topLeftX = topLeftX;
		this.topLeftY = topLeftY;
		this.larghezzaSchermo = larghezzaSchermo;
		this.altezzaSchermo = altezzaSchermo;

        int larghezzaSingolaIcona = ImageCache.mappa.get(ClassiLocazione.BOSCO).getWidth();
		minOffsetPerNuvole = topLeftX + DIMENSIONE_BORDO_INTERNO_CORNICE_MAPPA;
		larghezzaRiquadroMappa = 7 * larghezzaSingolaIcona;
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
				int coordinateX = localXOffset + (x - daX) * mw;
				int coordinateY = localYOffset + (y - daY) * mh;
				BufferedImage image;
				if (x == gruppoX && y == gruppoY) {
					image = ImageCache.segnalino;
					if ((System.currentTimeMillis() / 1000) % 2 == 0) {
						graphics.drawImage(image, coordinateX, coordinateY, null);
					}
				} else {
					image = ImageCache.mappa.get(classeLocazione);
					graphics.drawImage(image, coordinateX, coordinateY, null);
					if (Foresta.isLocazioneVisitata(new CoordinateMD(x, y))) {
						scurisci(graphics, coordinateX, coordinateY, mw, mh, 50);
					}
				}
			}
		}

		// 2. Salva lo stato originale della Clip e del Composite
		Shape originalClip = graphics.getClip();
		Composite originalComposite = graphics.getComposite();

		// 3. APPLICA LA CLIP: Da adesso in poi si colora SOLO dentro questo quadrato
		graphics.clipRect(minOffsetPerNuvole, minOffsetPerNuvole, larghezzaRiquadroMappa, larghezzaRiquadroMappa);

		// (Opzionale) Per vedere dove finisce il riquadro
		//graphics.setColor(Color.RED); graphics.drawRect(minOffsetPerNuvole, minOffsetPerNuvole, larghezzaRiquadroMappa, larghezzaRiquadroMappa);

		// 4. Imposta la trasparenza e disegna le nuvole condivise con la mappa a tutto schermo
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
		CloudManager.assicuraGenerate(larghezzaSchermo, altezzaSchermo, mw, mh);
		CloudManager.disegna(graphics, daX, daY, localXOffset, localYOffset, mw, mh);

		// 5. RIPRISTINA TUTTO: Rimuove la clip e la trasparenza per i disegni successivi
		graphics.setComposite(originalComposite);
		graphics.setClip(originalClip);

		// Aggiorna la posizione delle nuvolette
		CloudManager.aggiorna();
	}

	private void scurisci(Graphics2D g, int x, int y, int width, int height, int percentualeOscuramento) {
		// Calcola alpha (0 = trasparente, 255 = nero opaco)
		int alpha = (int) (percentualeOscuramento * 2.55f);
		// Imposta il colore nero con la trasparenza calcolata
		g.setColor(new java.awt.Color(0, 0, 0, alpha));
		// Disegna il rettangolo sopra l'immagine
		g.fillRect(x, y, width, height);
	}

	SpriteInterface variaMappa() {
		BufferedImage icona = ImageCache.spriteMappa;
		int x = topLeftX + ((ImageCache.corniceMappa.getWidth() - ImageCache.spriteMappa.getWidth()) >> 1);
		int y = topLeftY + ((ImageCache.corniceMappa.getHeight() - ImageCache.spriteMappa.getHeight()) >> 1);
		return new SpriteATempo(icona, x, y);
	}
}
