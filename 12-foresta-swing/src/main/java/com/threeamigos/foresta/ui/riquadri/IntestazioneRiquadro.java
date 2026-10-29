package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;

import java.awt.*;

/**
 * L'intestazione di un riquadro della schermata di gioco, come quelle delle due liste dell'inventario: scritta grigia
 * chiara con l'ombra nera dietro, centrata sul bordo superiore della cornice (non toglie spazio al contenuto).
 */
final class IntestazioneRiquadro {

	private IntestazioneRiquadro() {
	}

	/**
	 * Disegna l'intestazione centrata sulla cornice larga {@code larghezzaCornice} che comincia in (xCornice, yCornice)
	 */
	static void disegna(Graphics2D graphics, String testo, int xCornice, int yCornice, int larghezzaCornice) {
		Image ombra = ImageCache.get(testo, DoomdarkColorModel.Color.BLACK);
		Image immagine = ImageCache.get(testo, DoomdarkColorModel.Color.LIGHT_GRAY);
		int x = xCornice + larghezzaCornice / 2 - immagine.getWidth(null) / 2;
		int y = yCornice + ImageCache.SPACING;
		graphics.drawImage(ombra, x + 2, y + 2, null);
		graphics.drawImage(immagine, x, y, null);
	}
}
