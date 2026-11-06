package com.threeamigos.foresta.ui.riquadri;

import com.threeamigos.foresta.ui.immagini.ImageCache;
import com.threeamigos.foresta.ui.testo.DoomdarkColorModel;
import com.threeamigos.foresta.ui.testo.DoomdarkFont;

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
		int x = PosizioneDellIntestazione.x(xCornice, larghezzaCornice, immagine.getWidth(null));
		int y = PosizioneDellIntestazione.y(yCornice);
		graphics.drawImage(ombra, x + PosizioneDellIntestazione.SCOSTAMENTO_OMBRA, y + PosizioneDellIntestazione.SCOSTAMENTO_OMBRA, null);
		graphics.drawImage(immagine, x, y, null);
	}
}
