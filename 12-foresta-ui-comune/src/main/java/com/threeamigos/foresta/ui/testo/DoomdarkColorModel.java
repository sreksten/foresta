package com.threeamigos.foresta.ui.testo;

/**
 * I colori con cui si scrive il testo bitmap. Il glifo è fatto di livelli di grigio che sono l'alfa del colore: ogni colore ha il suo
 * valore pieno (quello del primo piano), mentre lo sfondo è sempre trasparente. Come diventa un'immagine lo dice chi disegna, secondo il
 * suo toolkit (in Swing un modello a due indici, in libGDX una tinta).
 */
public final class DoomdarkColorModel {

	private DoomdarkColorModel() {
	}

	/**
	 * I colori del testo, dal bianco al nero passando per i grigi, più rosso, giallo, verde, arancione e il marrone del bordo dei cartigli.
	 */
	public enum Color {
		WHITE(0xFFFFFF),
		LIGHT_GRAY(0xADADAD),
		MEDIUM_GRAY(0x737373),
		DARK_GRAY(0x525252),
		VERY_DARK_GRAY(0x222222),
		BLACK(0x000000),
		RED(0xFF0000),
		YELLOW(0xFFFF00),
		GREEN(0x00FF00),
		// L'arancione dei punti abilità da spendere
		ORANGE(0xFF8C00),
		// Il marrone del bordo dei cartigli
		BROWN(0x8C4210);

		private final int rgb;

		Color(int rgb) {
			this.rgb = rgb;
		}

		/**
		 * Il colore pieno del testo, come 0xRRGGBB
		 */
		public int getRgb() {
			return rgb;
		}
	}
}
