package com.threeamigos.foresta.ui.sfx;

import com.threeamigos.foresta.motore.Foresta;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Genera e mantiene un unico set di nuvole condiviso tra le varie viste della mappa
 * (riquadro mappa e mappa a tutto schermo), in modo che risultino coerenti tra loro
 * quando si passa da una vista all'altra. La generazione è quella "storica" di
 * DisplayableCanvasMappaATuttoSchermo, basata sull'ingombro a schermo della mappa conosciuta.
 */
public class CloudManager {

	private static final int NUMERO_NUVOLE = 12;

	private static List<CloudInstance> clouds;
	private static Rectangle clipCanonico;
	// Cella della foresta (colonna/riga) a cui corrisponde l'angolo in alto a sinistra di clipCanonico
	private static int daXCanonico;
	private static int daYCanonico;
	private static int larghezzaIconaCanonica;
	private static int altezzaIconaCanonica;

	private CloudManager() {
	}

	public static void assicuraGenerate(int width, int height, int larghezzaIcona, int altezzaIcona) {
		if (clouds == null) {
			genera(width, height, larghezzaIcona, altezzaIcona);
		}
	}

	public static void aggiorna() {
		if (clouds != null) {
			for (CloudInstance cloud : clouds) {
				cloud.update();
			}
		}
	}

	/**
	 * Disegna le nuvole traslando le loro coordinate (definite rispetto alla mappa
	 * a tutto schermo, la vista "di riferimento") in base alla porzione di foresta
	 * e alla posizione a schermo mostrate dal canvas chiamante. Nessuno scaling:
	 * le nuvole hanno la stessa dimensione ovunque, solo la loro posizione viene
	 * riallineata in modo che una nuvola sopra una data cella della foresta appaia
	 * nello stesso punto relativo sia nella mappa a tutto schermo sia nel riquadro
	 * mappa in-game (che ne mostra solo una finestra più piccola, centrata sul
	 * giocatore).
	 *
	 * @param daXVista       colonna della foresta mostrata all'angolo in alto a sinistra della vista
	 * @param daYVista       riga della foresta mostrata all'angolo in alto a sinistra della vista
	 * @param xVista         coordinata schermo X di quell'angolo in alto a sinistra
	 * @param yVista         coordinata schermo Y di quell'angolo in alto a sinistra
	 * @param larghezzaIcona larghezza in pixel di una cella della mappa in questa vista
	 * @param altezzaIcona   altezza in pixel di una cella della mappa in questa vista
	 */
	public static void disegna(Graphics2D graphics, int daXVista, int daYVista, int xVista, int yVista,
			int larghezzaIcona, int altezzaIcona) {
		if (clouds == null || clipCanonico == null) {
			return;
		}
		int deltaX = (xVista - daXVista * larghezzaIcona) - (clipCanonico.x - daXCanonico * larghezzaIconaCanonica);
		int deltaY = (yVista - daYVista * altezzaIcona) - (clipCanonico.y - daYCanonico * altezzaIconaCanonica);
		for (CloudInstance cloud : clouds) {
			graphics.drawImage(cloud.getImage(), cloud.getX() + deltaX, cloud.getY() + deltaY, null);
		}
	}

	private static void genera(int width, int height, int larghezzaIcona, int altezzaIcona) {
		Random rand = new Random();

		int daX = Foresta.getMinXConosciuta();
		int aX = Foresta.getMaxXConosciuta();
		int daY = Foresta.getMinYConosciuta();
		int aY = Foresta.getMaxYConosciuta();

		int larghezzaMappa = (aX - daX + 1) * larghezzaIcona;
		int altezzaMappa = (aY - daY + 1) * altezzaIcona;
		int coordinataSchermoX = (width - larghezzaMappa) >> 1;
		int coordinataSchermoY = (height - altezzaMappa) >> 1;

		clipCanonico = new Rectangle(coordinataSchermoX, coordinataSchermoY, larghezzaMappa, altezzaMappa);
		daXCanonico = daX;
		daYCanonico = daY;
		larghezzaIconaCanonica = larghezzaIcona;
		altezzaIconaCanonica = altezzaIcona;

		clouds = new ArrayList<>();
		for (int i = 0; i < NUMERO_NUVOLE; i++) {
			int cloudWidth = (int) (clipCanonico.width * 0.3) + rand.nextInt((int) (clipCanonico.width * 0.3));
			int cloudHeight = cloudWidth / 2;

			BufferedImage singleCloudPattern = CloudGenerator.generateCloud(cloudWidth, cloudHeight);

			float startX = clipCanonico.x + rand.nextInt(clipCanonico.width) - cloudWidth;
			float startY = clipCanonico.y + rand.nextInt(clipCanonico.height - cloudHeight);

			float speed = 0.2f + ((float) cloudWidth / clipCanonico.width) * 0.8f;

			clouds.add(new CloudInstance(singleCloudPattern, startX, startY, speed, clipCanonico));
		}
	}
}
