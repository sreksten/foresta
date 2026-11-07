package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.threeamigos.foresta.ui.sprite.DescrizioneFumetto;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

/**
 * Il disegno di un fumetto in libGDX, fermo: un rettangolo bianco smussato con il testo e una punta che indica chi parla. Com'è fatta la nuvola e dov'è la
 * punta lo dice {@link DescrizioneFumetto}, nel modulo comune. Lo usano il fumetto dei riquadri ({@link SpriteFumettoGdx}, che lo fa salire e svanire) e le
 * pagine degli intermezzi, dove sta fermo finché la battuta è visibile.
 */
final class NuvolaDiFumettoGdx {

	/**
	 * Di quanti pixel si accorcia ogni riga dei quattro in cima (e in fondo) alla nuvola, per smussare gli angoli
	 */
	private static final int[] SMUSSO_DEGLI_ANGOLI = {2, 1, 0, 0};

	private NuvolaDiFumettoGdx() {
	}

	/**
	 * @param sinistra l'angolo in basso a sinistra della nuvola
	 * @param basso    la sua quota (il fumetto cresce verso l'alto)
	 */
	static void disegna(Batch batch, ImmaginiGdx immagini, TestoGdx testo, DescrizioneFumetto dati, DoomdarkColorModel.Color colore, int sinistra, int basso,
						int puntaVersoX, int puntaVersoY, float alpha) {
		disegna(batch, immagini, testo, dati, colore, sinistra, basso, dati.getLarghezza(), dati.getAltezza(), puntaVersoX, puntaVersoY, alpha);
	}

	/**
	 * Come sopra, ma con una nuvola più grande del testo (lo spazio per qualcos'altro, come i pulsanti del tutorial): il testo sta in alto a sinistra.
	 *
	 * @param larghezza la larghezza della nuvola, almeno quella del testo
	 * @param altezza   la sua altezza, almeno quella del testo
	 */
	static void disegna(Batch batch, ImmaginiGdx immagini, TestoGdx testo, DescrizioneFumetto dati, DoomdarkColorModel.Color colore, int sinistra, int basso,
						int larghezza, int altezza, int puntaVersoX, int puntaVersoY, float alpha) {
		int alto = basso - altezza;
		Color bianco = new Color(1f, 1f, 1f, alpha);

		// Il rettangolo smussato: quattro righe accorciate in cima e in fondo, e in mezzo il resto
		for (int riga = 0; riga < SMUSSO_DEGLI_ANGOLI.length; riga++) {
			int smusso = SMUSSO_DEGLI_ANGOLI[riga];
			immagini.riempi(batch, sinistra + smusso, alto + riga, larghezza - 2 * smusso, 1, bianco);
			immagini.riempi(batch, sinistra + smusso, basso - 1 - riga, larghezza - 2 * smusso, 1, bianco);
		}
		int righeDelCentro = altezza - 2 * SMUSSO_DEGLI_ANGOLI.length;
		if (righeDelCentro > 0) {
			immagini.riempi(batch, sinistra, alto + SMUSSO_DEGLI_ANGOLI.length, larghezza, righeDelCentro, bianco);
		}
		Color coloreDelTesto = TestoGdx.coloreConAlfa(colore, alpha);
		for (DescrizioneFumetto.Riga riga : dati.getRighe()) {
			testo.disegna(batch, riga.getTesto(), sinistra + riga.getX(), alto + riga.getY(), coloreDelTesto, 1f);
		}

		int[] punta = DescrizioneFumetto.punta(sinistra, basso, larghezza, altezza, puntaVersoX, puntaVersoY);
		if (punta != null) {
			riempiTriangolo(batch, immagini, punta, bianco);
		}
	}

	/**
	 * Riempie il triangolo {x1, y1, x2, y2, x3, y3} una riga di pixel per volta
	 */
	private static void riempiTriangolo(Batch batch, ImmaginiGdx immagini, int[] t, Color colore) {
		int[] xs = {t[0], t[2], t[4]};
		int[] ys = {t[1], t[3], t[5]};
		int minimo = Math.min(ys[0], Math.min(ys[1], ys[2]));
		int massimo = Math.max(ys[0], Math.max(ys[1], ys[2]));
		for (int y = minimo; y <= massimo; y++) {
			double centro = y + 0.5;
			double da = Double.MAX_VALUE;
			double a = -Double.MAX_VALUE;
			for (int lato = 0; lato < 3; lato++) {
				int altro = (lato + 1) % 3;
				double y1 = ys[lato];
				double y2 = ys[altro];
				if ((centro < Math.min(y1, y2)) || (centro > Math.max(y1, y2)) || y1 == y2) {
					continue;
				}
				double x = xs[lato] + (xs[altro] - xs[lato]) * (centro - y1) / (y2 - y1);
				da = Math.min(da, x);
				a = Math.max(a, x);
			}
			if (a >= da) {
				int sinistra = (int) Math.floor(da);
				immagini.riempi(batch, sinistra, y, Math.max(1, (int) Math.ceil(a) - sinistra), 1, colore);
			}
		}
	}
}
