package com.threeamigos.foresta.ui;

import com.threeamigos.foresta.ui.sfx.TracciatoreLogo;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Il primo contenuto della finestra, nello stato LOGO_INIZIALE: il logo 3AM tracciato una volta dal
 * {@link TracciatoreLogo}, centrato sulla finestra, su fondo nero: le scie svaniscono mentre il logo compare, poi il
 * logo resta da solo per due secondi e svanisce. Non usa ImageCache: il logo lo carica da se',
 * mentre le altre immagini si caricano in background.
 */
final class PannelloLogoIniziale extends JComponent {

	// Quanto resta il logo da solo, e in quanti fotogrammi svanisce (come la dissolvenza con cui compare)
	private static final int PAUSA_MS = 2_000;
	private static final int FOTOGRAMMI_SCOMPARSA = 40;

	private final TracciatoreLogo effetto;

	PannelloLogoIniziale(int larghezza, int altezza) {
		BufferedImage logo = BufferedImageBuilder.buildBufferedImage(ImageCache.RISORSA_LOGO_3AM);
		// Lo sfondo lo dipinge il pannello, su tutta la finestra. Le scie svaniscono mentre il logo compare, il logo
		// resta da solo per due secondi e poi svanisce anche lui: si passa al gioco da una finestra nera.
		effetto = TracciatoreLogo.costruttore(logo)
				.coloreSfondo(null)
				.sfumaScie(true)
				.fotogrammiPausa(PAUSA_MS / TracciatoreLogo.INTERVALLO_FOTOGRAMMA_MS)
				.fotogrammiScomparsa(FOTOGRAMMI_SCOMPARSA)
				.costruisci();
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
		g2.translate((getWidth() - effetto.getLarghezza()) / 2, (getHeight() - effetto.getAltezza()) / 2);
		effetto.disegna(g2);
		g2.dispose();
	}
}
