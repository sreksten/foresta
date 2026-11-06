package com.threeamigos.foresta.ui.schermate;

import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Il notiziario in fondo alla mappa a tutto schermo, in Swing: una copia della striscia di {@link StrisciaDelleNotizie} (nel modulo comune, che dice
 * cosa c'è scritto, dove e quanto è scorsa) in un'immagine sola, ripetuta affiancata (tiling), con lo sfondo blu e la dissolvenza a destra.
 */
class Notiziario {

	private static final Color COLORE_SFONDO = new Color(StrisciaDelleNotizie.SFONDO);

	private final StrisciaDelleNotizie striscia;
	private final int altezza;
	private BufferedImage immagineCorrente;
	private List<StrisciaDelleNotizie.Pezzo> pezziDellImmagine;

	Notiziario(StrisciaDelleNotizie striscia, int altezza) {
		this.striscia = striscia;
		this.altezza = altezza;
	}

	/**
	 * Disegna le notizie in una banda con l'angolo in quel punto; senza notizie non disegna niente.
	 */
	public void disegna(Graphics2D g, int xBanda, int yBanda) {
		striscia.avanza(System.nanoTime());
		if (striscia.isVuota()) {
			return;
		}
		int larghezza = striscia.getLarghezza();
		g.setColor(COLORE_SFONDO);
		g.fillRect(xBanda, yBanda, larghezza, altezza);

		if (immagineCorrente == null || pezziDellImmagine != striscia.getPezzi()) {
			costruisciImmagine();
		}
		if (immagineCorrente != null) {
			Shape clipOriginale = g.getClip();
			g.clipRect(xBanda, yBanda, larghezza, altezza);
			int yTesto = yBanda + (altezza - immagineCorrente.getHeight()) / 2;
			// Tiling: la stessa immagine (titolo+corpo+separatore di ogni notizia, concatenati) viene disegnata più volte affiancata, così la striscia
			// riprende subito da dove finisce l'ultima copia visibile
			int drawX = striscia.getX();
			while (drawX < larghezza) {
				g.drawImage(immagineCorrente, xBanda + drawX, yTesto, null);
				drawX += striscia.getLarghezzaTotale();
			}
			g.setClip(clipOriginale);
		}
		disegnaZonaFade(g, xBanda, yBanda, larghezza);
	}

	private void disegnaZonaFade(Graphics2D g, int xBanda, int yBanda, int larghezza) {
		int xInizioFade = xBanda + larghezza - StrisciaDelleNotizie.LARGHEZZA_ZONA_DI_DISSOLVENZA;
		Paint gradienteOriginale = g.getPaint();
		GradientPaint gradiente = new GradientPaint(
				xInizioFade, yBanda,
				new Color(COLORE_SFONDO.getRed(), COLORE_SFONDO.getGreen(), COLORE_SFONDO.getBlue(), 0),
				xBanda + larghezza, yBanda, COLORE_SFONDO);
		g.setPaint(gradiente);
		g.fillRect(xInizioFade, yBanda, StrisciaDelleNotizie.LARGHEZZA_ZONA_DI_DISSOLVENZA, altezza);
		g.setPaint(gradienteOriginale);
	}

	/**
	 * Una copia della striscia in un'immagine sola, dai pezzi del modello.
	 */
	private void costruisciImmagine() {
		pezziDellImmagine = striscia.getPezzi();
		if (pezziDellImmagine.isEmpty() || striscia.getLarghezzaTotale() <= 0) {
			immagineCorrente = null;
			return;
		}
		DoomdarkFont font = striscia.getFont();
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		BufferedImage immagine = gc.createCompatibleImage(striscia.getLarghezzaTotale(), font.getHeight(), Transparency.TRANSLUCENT);
		Graphics2D g2d = immagine.createGraphics();
		for (StrisciaDelleNotizie.Pezzo pezzo : pezziDellImmagine) {
			g2d.drawImage(ImageCache.get(pezzo.getTesto(), font, pezzo.getColore()), pezzo.getX(), 0, null);
		}
		g2d.dispose();
		immagineCorrente = immagine;
	}
}
