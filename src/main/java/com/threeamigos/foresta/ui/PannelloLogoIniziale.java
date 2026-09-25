package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.ui.sfx.TracciatoreLogo;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Il primo contenuto della finestra, nello stato LOGO_INIZIALE: il logo 3AM tracciato una volta dal
 * {@link TracciatoreLogo}, ingrandito del doppio e centrato sulla finestra, su fondo nero. Non usa ImageCache: il logo lo carica da se',
 * mentre le altre immagini si caricano in background.
 */
final class PannelloLogoIniziale extends JComponent {

	// Il logo 3AM e' piccolo (212x100) rispetto alla finestra: lo si disegna ingrandito
	private static final int SCALA = 2;

	private final TracciatoreLogo effetto;

	PannelloLogoIniziale(int larghezza, int altezza) {
		BufferedImage logo = BufferedImageBuilder.buildBufferedImage(ImageCache.RISORSA_LOGO_3AM);
		// Lo sfondo lo dipinge il pannello, su tutta la finestra
		effetto = TracciatoreLogo.costruttore(logo).coloreSfondo(null).costruisci();
		setSize(larghezza, altezza);
		setOpaque(true);
	}

	/** Un fotogramma dell'animazione. */
	void avanza() {
		effetto.avanza();
		repaint();
	}

	boolean isFinito() {
		return effetto.isFinito();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.translate((getWidth() - SCALA * effetto.getLarghezza()) / 2, (getHeight() - SCALA * effetto.getAltezza()) / 2);
		g2.scale(SCALA, SCALA);
		effetto.disegna(g2);
		g2.dispose();
	}
}
