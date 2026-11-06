package com.threeamigos.foresta.ui.sfx;

import com.threeamigos.foresta.ui.immagini.ImmaginiAwt;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Disegna in Swing le nuvole del {@link GestoreNuvole}: ogni nuvola (un'{@link ImmagineDiPixel}) diventa una volta sola un'immagine di Swing, ricordata finché
 * la nuvola esiste.
 */
public final class NuvoleAwt {

	private static final Map<ImmagineDiPixel, BufferedImage> IMMAGINI = new WeakHashMap<>();

	private NuvoleAwt() {
	}

	/**
	 * Disegna le nuvole per una vista (vedi {@link GestoreNuvole#nuvolePerLaVista})
	 */
	public static void disegna(Graphics2D graphics, int daXVista, int daYVista, int xVista, int yVista, int larghezzaIcona, int altezzaIcona) {
		for (GestoreNuvole.NuvolaVisibile nuvola : GestoreNuvole.nuvolePerLaVista(daXVista, daYVista, xVista, yVista, larghezzaIcona, altezzaIcona)) {
			BufferedImage immagine;
			synchronized (IMMAGINI) {
				immagine = IMMAGINI.computeIfAbsent(nuvola.getImmagine(), ImmaginiAwt::buffered);
			}
			graphics.drawImage(immagine, nuvola.getX(), nuvola.getY(), null);
		}
	}
}
