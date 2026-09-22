package com.threeamigos.foresta.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Sfera magica decorativa ancorata all'angolo inferiore sinistro di tutto il
 * canvas di gioco, visibile solo mentre è mostrata la mappa a tutto schermo
 * (vedi {@code ForestaUI.gestisciEventoRichiestaVisualizzazioneMappa} e
 * {@code ForestaUI.gestisciEventoMostraSchermataGioco}). È più alta della
 * sola fascia di {@link PannelloIcone}, quindi copre anche l'angolo inferiore
 * sinistro di {@link DisplayableCanvas} (e con esso il {@link Notiziario} che
 * vi scorre, tagliato apposta per non disegnare sotto di lei): per questo va
 * aggiunta alla layered pane del {@link JFrame} invece che al suo content
 * pane, così da restare sempre disegnata sopra {@link DisplayableCanvas} e
 * {@link PannelloIcone} anche quando l'animazione continua del primo la
 * sovrappone (vedi {@code ForestaUI.creaEMostraInterfacciaUtente}).
 */
class PannelloSferaMagica extends JPanel {

	private static final long serialVersionUID = 1L;

	PannelloSferaMagica() {
		setOpaque(false);
		setSize(ImageCache.sferaMagica.getWidth(), ImageCache.sferaMagica.getHeight());
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(ImageCache.sferaMagica, 0, 0, null);
	}
}
