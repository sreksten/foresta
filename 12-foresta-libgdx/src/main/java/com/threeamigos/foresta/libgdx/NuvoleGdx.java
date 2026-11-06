package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import com.threeamigos.foresta.ui.sfx.GestoreNuvole;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Disegna in libGDX le nuvole del {@link GestoreNuvole}: ogni nuvola (un'{@link ImmagineDiPixel} generata senza toolkit) diventa una sola volta una texture
 * di {@link ImmaginiGdx}, che la libera. Le coordinate sono quelle di Swing (asse y verso il basso).
 */
public final class NuvoleGdx {

	private final ImmaginiGdx immagini;
	private final Map<ImmagineDiPixel, TextureRegion> regioni = new IdentityHashMap<>();

	public NuvoleGdx(ImmaginiGdx immagini) {
		this.immagini = immagini;
	}

	/**
	 * Disegna le nuvole per una vista (vedi {@link GestoreNuvole#nuvolePerLaVista}); il colore e la trasparenza sono quelli del batch
	 */
	public void disegna(Batch batch, int daXVista, int daYVista, int xVista, int yVista, int larghezzaIcona, int altezzaIcona) {
		for (GestoreNuvole.NuvolaVisibile nuvola : GestoreNuvole.nuvolePerLaVista(daXVista, daYVista, xVista, yVista, larghezzaIcona, altezzaIcona)) {
			// Le nuvole sono sfumature: il campionamento è lineare, a differenza del pixel-art
			TextureRegion regione = regioni.computeIfAbsent(nuvola.getImmagine(), immagine -> immagini.regioneDaPixel(immagine, true));
			batch.draw(regione, nuvola.getX(), nuvola.getY());
		}
	}
}
