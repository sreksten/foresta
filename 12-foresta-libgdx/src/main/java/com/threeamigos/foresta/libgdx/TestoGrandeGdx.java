package com.threeamigos.foresta.libgdx;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;
import com.threeamigos.foresta.ui.testo.LayoutDelTestoGrande;

import java.util.HashMap;
import java.util.Map;

/**
 * Il testo nell'alfabeto grande in libGDX (la storia, i titoli, i punteggi della sfida): le misure, l'andare a capo, la composizione e l'alone sono del modulo
 * comune ({@link LayoutDelTestoGrande}, gli stessi di Swing); qui l'immagine di pixel diventa una texture di {@link ImmaginiGdx}, una sola volta per ogni testo
 * (le scritte sono poche e quasi sempre le stesse).
 */
public final class TestoGrandeGdx {

	private final ImmaginiGdx immagini;
	private final Map<String, TextureRegion> cache = new HashMap<>();

	public TestoGrandeGdx(ImmaginiGdx immagini) {
		this.immagini = immagini;
	}

	/**
	 * Il testo in un'immagine larga quanto la larghezza massima, a capo dove serve, centrato o allineato a sinistra, con un alone nero di quel raggio attorno
	 * alle lettere (0 per nessun alone: allora l'immagine ha la larghezza chiesta, altrimenti più larga di due raggi)
	 */
	public TextureRegion immagine(String testo, int larghezzaMassima, boolean centrato, int raggioDellAlone) {
		String chiave = testo + "|" + larghezzaMassima + "|" + centrato + "|" + raggioDellAlone;
		TextureRegion regione = cache.get(chiave);
		if (regione == null) {
			ImmagineDiPixel pixel = LayoutDelTestoGrande.immagine(testo, larghezzaMassima, centrato);
			if (raggioDellAlone > 0) {
				pixel = LayoutDelTestoGrande.conAlone(pixel, raggioDellAlone);
			}
			regione = immagini.regioneDaPixel(pixel, false);
			cache.put(chiave, regione);
		}
		return regione;
	}
}
