package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.motore.Logger;
import com.threeamigos.foresta.motore.Notizie;
import com.threeamigos.foresta.motore.modellodati.Notizia;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Ticker delle notizie in fondo alla mappa a tutto schermo: mostra le
 * {@link Notizie#getUltimeNotizie()} come un'unica striscia composita
 * (titolo in giallo + corpo in bianco con {@link DoomdarkFontMedium},
 * separate da un "-" con margini di {@value #LARGHEZZA_MARGINE_SEPARATORE_NOTIZIE}px,
 * incluso dopo l'ultima per un loop continuo), disegnata più volte affiancata (tiling) così che una
 * notizia comincia a comparire da destra mentre la precedente sta ancora
 * uscendo da sinistra, senza mai fermarsi. Nessun parametro di delta-time:
 * come il resto della UI (vedi {@link SpriteBase}), avanza di un passo
 * fisso a ogni chiamata di {@link #disegna}, invocata una volta per repaint.
 */
class Notiziario {

	private static final int VELOCITA_SCROLL_PX_PER_FRAME = 3;
	private static final int LARGHEZZA_ZONA_FADE = 60;
	private static final int LARGHEZZA_MARGINE_SEPARATORE_NOTIZIE = 16;
	private static final Color COLORE_SFONDO = new Color(20, 40, 120);
	private static final DoomdarkFont FONT = DoomdarkFontMedium.getInstance();
	private static final String SEPARATORE_TITOLO = " - ";

	private final int larghezza;
	private final int altezza;

	private List<Notizia> notizieCostruite = new ArrayList<>();
	private BufferedImage immagineCorrente;
	private float x;

	Notiziario(int larghezza, int altezza) {
		this.larghezza = larghezza;
		this.altezza = altezza;
	}

	void disegna(Graphics2D g, int xBanda, int yBanda) {
		List<Notizia> notizieAttuali = Notizie.getUltimeNotizie();
		if (notizieAttuali.isEmpty()) {
			return;
		}

		g.setColor(COLORE_SFONDO);
		g.fillRect(xBanda, yBanda, larghezza, altezza);

		if (immagineCorrente == null || !notizieAttuali.equals(notizieCostruite)) {
			costruisciImmagineComplessiva(notizieAttuali);
		}

		if (immagineCorrente != null) {
			Shape clipOriginale = g.getClip();
			g.clipRect(xBanda, yBanda, larghezza, altezza);
			int yTesto = yBanda + (altezza - immagineCorrente.getHeight()) / 2;
			int larghezzaTotale = immagineCorrente.getWidth();
			// Tiling: la stessa immagine (titolo+corpo+separatore di ogni notizia,
			// concatenati) viene disegnata più volte affiancata, così la striscia
			// riprende subito da dove finisce l'ultima copia visibile, senza mai
			// attendere che una notizia sia uscita del tutto dallo schermo.
			int drawX = Math.round(x);
			while (drawX < larghezza) {
				g.drawImage(immagineCorrente, xBanda + drawX, yTesto, null);
				drawX += larghezzaTotale;
			}
			g.setClip(clipOriginale);

			x -= VELOCITA_SCROLL_PX_PER_FRAME;
			while (x <= -larghezzaTotale) {
				x += larghezzaTotale;
			}
		}

		disegnaZonaFade(g, xBanda, yBanda);
	}

	private void disegnaZonaFade(Graphics2D g, int xBanda, int yBanda) {
		int xInizioFade = xBanda + larghezza - LARGHEZZA_ZONA_FADE;
		Paint gradienteOriginale = g.getPaint();
		GradientPaint gradiente = new GradientPaint(
				xInizioFade, yBanda,
				new Color(COLORE_SFONDO.getRed(), COLORE_SFONDO.getGreen(), COLORE_SFONDO.getBlue(), 0),
				xBanda + larghezza, yBanda, COLORE_SFONDO);
		g.setPaint(gradiente);
		g.fillRect(xInizioFade, yBanda, LARGHEZZA_ZONA_FADE, altezza);
		g.setPaint(gradienteOriginale);
	}

	private void costruisciImmagineComplessiva(List<Notizia> notizieAttuali) {
		List<BufferedImage> segmenti = new ArrayList<>();
		int larghezzaTotale = 0;
		// Il "-" con i suoi margini è disegnato come immagine dedicata invece che
		// come testo " - ": gli spazi del font Doomdark sono larghi solo pochi
		// pixel, troppo poco per separare visibilmente due notizie concatenate.
		BufferedImage separatore = costruisciSeparatore();
		for (Notizia notizia : notizieAttuali) {
			try {
				BufferedImage segmento = costruisciImmagine(notizia);
				segmenti.add(segmento);
				larghezzaTotale += segmento.getWidth();
				segmenti.add(separatore);
				larghezzaTotale += separatore.getWidth();
			} catch (DoomdarkFont.UnsupportedCharacterException e) {
				Logger.log(e);
			}
		}

		// Snapshot per valore: Notizie.getUltimeNotizie() restituisce la lista viva,
		// quindi va copiata per poterla confrontare più avanti con il suo stato futuro.
		notizieCostruite = new ArrayList<>(notizieAttuali);

		if (segmenti.isEmpty()) {
			immagineCorrente = null;
			return;
		}

		boolean primaCostruzione = immagineCorrente == null;

		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration();
		BufferedImage immagine = gc.createCompatibleImage(larghezzaTotale, FONT.getHeight(), Transparency.TRANSLUCENT);
		Graphics2D g2d = immagine.createGraphics();
		int xSegmento = 0;
		for (BufferedImage segmento : segmenti) {
			g2d.drawImage(segmento, xSegmento, 0, null);
			xSegmento += segmento.getWidth();
		}
		g2d.dispose();
		immagineCorrente = immagine;

		// L'ingresso "da destra" con dissolvenza si applica solo alla primissima
		// notizia mai mostrata: le costruzioni successive (nuova notizia arrivata,
		// o lista svuotata e ripopolata) proseguono lo scroll già in corso.
		if (primaCostruzione) {
			x = larghezza;
		}
	}

	private BufferedImage costruisciSeparatore() {
		Image imgTrattino = DoomdarkTextProducer.getImage("-", FONT, DoomdarkColorModel.Color.WHITE);
		int larghezzaTrattino = imgTrattino.getWidth(null);
		int larghezzaTotale = LARGHEZZA_MARGINE_SEPARATORE_NOTIZIE * 2 + larghezzaTrattino;

		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration();
		BufferedImage immagine = gc.createCompatibleImage(larghezzaTotale, FONT.getHeight(), Transparency.TRANSLUCENT);
		Graphics2D g2d = immagine.createGraphics();
		g2d.drawImage(imgTrattino, LARGHEZZA_MARGINE_SEPARATORE_NOTIZIE, 0, null);
		g2d.dispose();
		return immagine;
	}

	private BufferedImage costruisciImmagine(Notizia notizia) {
		String corpo = notizia.getCorpo();
		int posizioneSeparatore = corpo.indexOf(SEPARATORE_TITOLO);

		Image imgTitolo = null;
		String resto;
		if (posizioneSeparatore > 0) {
			String titolo = corpo.substring(0, posizioneSeparatore);
			resto = corpo.substring(posizioneSeparatore);
			imgTitolo = DoomdarkTextProducer.getImage(titolo, FONT, DoomdarkColorModel.Color.YELLOW);
		} else {
			resto = corpo;
		}
		Image imgResto = DoomdarkTextProducer.getImage(resto, FONT, DoomdarkColorModel.Color.WHITE);

		int larghezzaTitolo = imgTitolo == null ? 0 : imgTitolo.getWidth(null);
		int larghezzaTotale = larghezzaTitolo + imgResto.getWidth(null);

		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration();
		BufferedImage immagine = gc.createCompatibleImage(larghezzaTotale, FONT.getHeight(), Transparency.TRANSLUCENT);
		Graphics2D g2d = immagine.createGraphics();
		if (imgTitolo != null) {
			g2d.drawImage(imgTitolo, 0, 0, null);
		}
		g2d.drawImage(imgResto, larghezzaTitolo, 0, null);
		g2d.dispose();
		return immagine;
	}
}
