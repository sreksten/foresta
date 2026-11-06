package com.threeamigos.foresta.ui.sfx;

import com.threeamigos.foresta.ui.Area;
import com.threeamigos.foresta.ui.immagini.ImmagineDiPixel;

import java.util.Random;

/**
 * Una nuvola sulla mappa: la sua immagine, la posizione e la velocità; si muove verso destra e quando esce dal riquadro rientra da sinistra a
 * un'altezza diversa.
 */
public final class Nuvola {

	private final ImmagineDiPixel immagine;
	private float x;
	private float y;
	private final float velocita; // pixel al secondo
	private final Area limiti; // Il riquadro in cui la nuvola può muoversi
	private final Random caso = new Random();

	public Nuvola(ImmagineDiPixel immagine, float xIniziale, float yIniziale, float velocita, Area limiti) {
		this.immagine = immagine;
		this.x = xIniziale;
		this.y = yIniziale;
		this.velocita = velocita;
		this.limiti = limiti;
	}

	/**
	 * Fa avanzare la nuvola del tempo reale trascorso dall'ultima volta, non di un passo fisso per chiamata: l'aggiornamento può essere chiamato anche da un
	 * ridisegno estraneo all'animazione (per esempio il passaggio del mouse).
	 */
	public void aggiorna(float secondiTrascorsi) {
		x += velocita * secondiTrascorsi;

		// Confine destro del riquadro (x + larghezza)
		int destra = limiti.getX() + limiti.getLarghezza();

		// Se la nuvola supera il confine destro del riquadro si rimette appena prima del bordo sinistro
		if (x > destra) {
			x = limiti.getX() - immagine.getLarghezza();

			// Una nuova altezza casuale, ma sempre dentro il riquadro
			int altezzaMassimaDisponibile = limiti.getAltezza() - immagine.getAltezza();
			if (altezzaMassimaDisponibile > 0) {
				y = limiti.getY() + caso.nextInt(altezzaMassimaDisponibile);
			} else {
				y = limiti.getY(); // Se la nuvola è più alta del riquadro stesso
			}
		}
	}

	public ImmagineDiPixel getImmagine() {
		return immagine;
	}

	public int getX() {
		return (int) x;
	}

	public int getY() {
		return (int) y;
	}
}
