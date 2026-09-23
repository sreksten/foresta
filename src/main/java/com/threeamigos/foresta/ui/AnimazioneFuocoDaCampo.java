package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.intermezzi.StatoElemento;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Un fuoco da campo disegnato via codice, senza risorse grafiche: due ciocchi incrociati
 * e tre strati di fiamma (rosso, arancio, giallo) le cui punte oscillano a ritmi diversi,
 * più qualche scintilla. Gli otto fotogrammi sono preparati nel costruttore e ripetuti
 * a dieci al secondo.
 */
class AnimazioneFuocoDaCampo implements AnimazioneImmagine {

	private static final int LARGHEZZA = 48;
	private static final int ALTEZZA = 60;
	private static final int FOTOGRAMMI = 8;
	private static final double FOTOGRAMMI_AL_SECONDO = 10;

	private static final Color ROSSO = new Color(200, 40, 10);
	private static final Color ARANCIO = new Color(245, 130, 20);
	private static final Color GIALLO = new Color(255, 225, 90);
	private static final Color CIOCCO = new Color(100, 60, 30);
	private static final Color CIOCCO_SCURO = new Color(65, 38, 18);

	private final BufferedImage[] fotogrammi = new BufferedImage[FOTOGRAMMI];

	AnimazioneFuocoDaCampo() {
		for (int i = 0; i < FOTOGRAMMI; i++) {
			fotogrammi[i] = disegnaFotogramma(i);
		}
	}

	@Override
	public BufferedImage getFotogramma(double secondi, StatoElemento stato) {
		int indice = (int) Math.floor(Math.max(0, secondi) * FOTOGRAMMI_AL_SECONDO) % FOTOGRAMMI;
		return fotogrammi[indice];
	}

	private static BufferedImage disegnaFotogramma(int indice) {
		BufferedImage immagine = new BufferedImage(LARGHEZZA, ALTEZZA, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = immagine.createGraphics();
		double fase = 2 * Math.PI * indice / FOTOGRAMMI;

		// Fiamme, dalla più esterna alla più interna
		disegnaFiamma(g, ROSSO, 20, 46, fase, 0);
		disegnaFiamma(g, ARANCIO, 14, 34, fase, 1.7);
		disegnaFiamma(g, GIALLO, 8, 20, fase, 3.1);

		// Scintille che salgono
		g.setColor(GIALLO);
		for (int s = 0; s < 3; s++) {
			double salita = ((indice + s * 3) % FOTOGRAMMI) / (double) FOTOGRAMMI;
			int x = LARGHEZZA / 2 + (int) Math.round(Math.sin(fase + s * 2.1) * 8) + (s - 1) * 5;
			int y = ALTEZZA - 16 - (int) Math.round(salita * 38);
			g.fillRect(x, y, 2, 2);
		}

		// Ciocchi incrociati alla base, davanti alle fiamme
		g.setColor(CIOCCO_SCURO);
		g.fill(new Polygon(new int[] {4, 10, 44, 38}, new int[] {ALTEZZA - 10, ALTEZZA - 14, ALTEZZA - 4, ALTEZZA}, 4));
		g.setColor(CIOCCO);
		g.fill(new Polygon(new int[] {38, 44, 10, 4}, new int[] {ALTEZZA - 14, ALTEZZA - 10, ALTEZZA, ALTEZZA - 4}, 4));

		g.dispose();
		return immagine;
	}

	/**
	 * Una lingua di fuoco: larga alla base, con la punta che ondeggia di lato e cambia
	 * altezza nel tempo; lo sfasamento distingue gli strati.
	 */
	private static void disegnaFiamma(Graphics2D g, Color colore, int semilarghezza, int altezza, double fase, double sfasamento) {
		int centro = LARGHEZZA / 2;
		int base = ALTEZZA - 8;
		int altezzaPunta = altezza + (int) Math.round(Math.sin(fase * 2 + sfasamento) * altezza * 0.15);
		int scartoPunta = (int) Math.round(Math.sin(fase + sfasamento) * semilarghezza * 0.4);
		int scartoSpalle = (int) Math.round(Math.cos(fase * 3 + sfasamento) * semilarghezza * 0.2);
		Polygon fiamma = new Polygon();
		fiamma.addPoint(centro - semilarghezza, base);
		fiamma.addPoint(centro - semilarghezza * 3 / 4 + scartoSpalle, base - altezza / 2);
		fiamma.addPoint(centro + scartoPunta, base - altezzaPunta);
		fiamma.addPoint(centro + semilarghezza * 3 / 4 + scartoSpalle, base - altezza / 2);
		fiamma.addPoint(centro + semilarghezza, base);
		g.setColor(colore);
		g.fill(fiamma);
	}
}
